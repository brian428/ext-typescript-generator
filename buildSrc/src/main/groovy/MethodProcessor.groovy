class MethodProcessor
{
	TypeManager typeManager
	Config config
	DefinitionWriter definitionWriter
	ISpecialCases specialCases

	def init() {
	}

	def writeMethods( fileJson, processedNames, isInterface, useExport ) {
		def classMethods

		if( fileJson.members instanceof Map ) {
			classMethods = fileJson.members?.method
		}
		else {
			classMethods = fileJson.members.findAll{ m -> m.tagname == "method" }
		}

		def optionalFlag = isInterface ? "?" : ""
		def shouldUseExport = useExport
		def isSingleton = fileJson.singleton && !isInterface

		classMethods.each { thisMethod ->

			if( !isInterface || ( isInterface && thisMethod.name != "constructor" ) ) {
				def forceInclude = specialCases.shouldForceInclude( fileJson.name, thisMethod.name )

				if( typeManager.isOwner( fileJson, thisMethod?.owner ) || ( fileJson.mixins && thisMethod.owner in fileJson.mixins ) || forceInclude ) {
					if( ( !config.includePrivate && thisMethod.private != true ) || forceInclude || thisMethod.meta?.protected || thisMethod.meta?.template ) {
						if( !processedNames[ thisMethod.name ] && !thisMethod?.meta?.deprecated && !specialCases.shouldRemoveMethod( fileJson.name, thisMethod.name ) ) {

							processedNames[ thisMethod.name ] = true

							if( !thisMethod.shortDoc && thisMethod.short_doc )
								thisMethod.shortDoc = thisMethod.short_doc

							// Convert return types for special cases where original return type isn't valid
							if( specialCases.getReturnTypeOverride( thisMethod.return?.type ) ) {
								thisMethod.return.type = specialCases.getReturnTypeOverride( thisMethod.return.type )
							}

							// Convert return types for special cases where an overridden subclass method returns an invalid type
							if( thisMethod.return && specialCases.getReturnTypeOverride( fileJson.name, thisMethod.name ) ) {
								thisMethod.return.type = specialCases.getReturnTypeOverride( fileJson.name, thisMethod.name )
							}

							// Convert methods to property fields for special cases where an item has incompatible ExtJS API overrides in subclasses
							if( specialCases.shouldConvertToProperty( fileJson.name, thisMethod.name ) ) {
								definitionWriter.writeToDefinition( "\t\t/** [Method] ${ definitionWriter.formatCommentText( thisMethod.shortDoc ) } */" )
								definitionWriter.writeToDefinition( "\t\t${ thisMethod.name.replaceAll( '-', '' ) }${ optionalFlag }: any;" )
							}
							else {

								def tokenizedTypes = config.useFullTyping ? typeManager.getTokenizedReturnTypes( thisMethod.return?.type ) : [ "any" ]

								tokenizedTypes.each { thisType ->

									// Return type conversions
									if( thisType == "undefined" ) thisType = "void"

									def paramNames = []
									def paramTypes = []
									def rawParamTypes = [:]
									def requiresOverrides = false

									thisMethod.params.each { thisParam ->
										paramNames.add( [ name:thisParam.name, optional:thisParam.optional, doc:thisParam.doc, default:thisParam.default ] )

										if( config.useFullTyping ) {
											def tokenizedParamTypes = typeManager.getTokenizedTypes( thisParam.type )
											if( tokenizedParamTypes.size() > 1 && !requiresOverrides ) {
												requiresOverrides = true
											}
											paramTypes.add( tokenizedParamTypes )
										}
										else {
											paramTypes.add( thisParam.type )
										}

										rawParamTypes[ thisParam.name ] = thisParam.type
									}

									def methodWritten = false
									def usedPermutations = [:]

									if( !config.useFullTyping || !paramNames.size() ) {
										writeMethod( thisMethod.shortDoc, thisMethod.name, optionalFlag, paramNames, paramTypes, rawParamTypes, thisType, isInterface, shouldUseExport, isSingleton )
									}
									else if( config.useFullTyping && requiresOverrides && tokenizedTypes.first() == thisType ) {
										def overrideTypes = []
										paramNames.each { thisParamName ->
											overrideTypes.add( "any" )
										}
										writeMethod( thisMethod.shortDoc, thisMethod.name, optionalFlag, paramNames, overrideTypes, rawParamTypes, "any", isInterface, shouldUseExport, isSingleton )
										usedPermutations[ overrideTypes.join( ',' ) ] = true
										methodWritten = true
									}

									if( config.useFullTyping ) {
										def paramPermutations = GroovyCollections.combinations( paramTypes )

										paramPermutations.each { thisPermutation ->
											if( !requiresOverrides || ( requiresOverrides && thisPermutation.count{ typeManager.normalizeType( it ) == "any" } < thisPermutation.size() ) ) {
												def thisPermutationAsString = thisPermutation.join( ',' )
												if( !usedPermutations[ thisPermutationAsString ] ) {
													writeMethod( thisMethod.shortDoc, thisMethod.name, optionalFlag, paramNames, thisPermutation, rawParamTypes, thisType, isInterface, shouldUseExport, isSingleton, methodWritten )
													usedPermutations[ thisPermutationAsString ] = true
													methodWritten = true
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	def writeMethod( comment, methodName, optionalFlag, paramNames, paramTypes, rawParamTypes, returnType, isInterface, useExport, isStatic=false, omitComment=false ) {
		def paramsContainSpread = paramTypes.count{ ( it.startsWith( "..." ) || it.endsWith( "..." ) ) } > 0
		def exportString = useExport ? "export function " : ""
		def staticString = ( isStatic && !useExport && methodName != "constructor" ) ? "static " : ""
		comment = "\t\t/** [Method] ${ comment?.replaceAll( "[\\W]", " " ) }"

		def paramsDoc = ""

		if( !config.interfaceOnly || ( config.interfaceOnly && ( isInterface || useExport || isStatic ) ) ) {
			if( config.interfaceOnly )
				returnType = typeManager.convertToInterface( returnType )

			def methodOutput = "${ staticString }${ methodName }${ optionalFlag }("

			paramNames.eachWithIndex { thisParam, i ->
				def thisParamType = typeManager.convertToInterface( paramTypes[ i ] )
				def thisParamName = thisParam.name

				paramsDoc += "\t\t* @param ${ thisParamName } ${ rawParamTypes[ thisParamName ] } ${ definitionWriter.formatCommentText( thisParam.doc ) }"
				if( thisParam.doc && thisParam.doc.contains( "Optional " ) ) thisParam.optional = true

				def spread = ""
				if( paramNames.last() == thisParam && ( thisParamType.startsWith( "..." ) || thisParamType.endsWith( "..." ) ) ) {
					spread = "..."
				}

				// Param name conversions
				if( thisParamName == "class" ) thisParamName = "clazz"

				def optionalParamFlag = ( thisParam.optional || spread.size() > 0 ) ? "?" : ""
				if( spread.size() == 0 && config.forceAllParamsToOptional ) {
					optionalParamFlag = "?"
				}
				if( paramsContainSpread ) {
					optionalParamFlag = ""
				}

				methodOutput += " ${spread}${ thisParamName }${ optionalParamFlag }:${ spread.size() > 0 ? "any[]" : typeManager.normalizeType( thisParamType ) }"

				if( thisParam == paramNames.last() ) {
					methodOutput += " "
				}
				else {
					methodOutput += ","
					paramsDoc += "\n"
				}
			}

			if( !config.omitOverrideComments || ( config.omitOverrideComments && !omitComment ) ) {
				if( paramsDoc.length() > 0 ) {
					definitionWriter.writeToDefinition( comment )
					definitionWriter.writeToDefinition( paramsDoc )
					definitionWriter.writeToDefinition( "\t\t*/" )
				}
				else {
					definitionWriter.writeToDefinition( "${ comment }*/" )
				}
			}

			if( methodName != "constructor" ) {
				definitionWriter.writeToDefinition( "\t\t${exportString}${ methodOutput }): ${ typeManager.normalizeType( returnType ) };" )
			}
			else {
				definitionWriter.writeToDefinition( "\t\t${ methodOutput });" )
			}
		}
	}
}
