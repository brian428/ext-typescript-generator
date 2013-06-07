class MethodProcessor
{
	TypeManager typeManager
	Config config
	DefinitionWriter definitionWriter
	ISpecialCases specialCases

	String optionalFlag
	Boolean shouldUseExport
	Boolean isSingleton
	Boolean isInterface
	Map processedNames

	def init() {
	}

	def writeMethods( fileJson, alreadyProcessedNames, isAnInterface, useExport ) {
		processedNames = alreadyProcessedNames
		shouldUseExport = useExport
		isInterface = isAnInterface
		isSingleton = fileJson.singleton && !isInterface
		optionalFlag = isInterface ? "?" : ""

		def classMethods

		if( fileJson.members instanceof Map ) {
			classMethods = fileJson.members?.method
		}
		else {
			classMethods = fileJson.members.findAll{ m -> m.tagname == "method" }
		}

		classMethods.each { thisMethod ->

			if( shouldIncludeMethod( fileJson, thisMethod ) || specialCases.shouldForceInclude( fileJson.name, thisMethod.name ) ) {

				processedNames[ thisMethod.name ] = true
				normalizeMethodDoc( thisMethod )
				normalizeReturnType( thisMethod, fileJson )

				// Convert methods to property fields for special cases where an item has incompatible ExtJS API overrides in subclasses
				if( specialCases.shouldConvertToProperty( fileJson.name, thisMethod.name ) ) {
					writeMethodAsProperty( thisMethod )
				}
				else {
					iterateMethodSignatures( thisMethod )
				}
			}
		}
	}

	def iterateMethodSignatures( thisMethod ) {
		def tokenizedTypes = config.useFullTyping ? typeManager.getTokenizedReturnTypes( thisMethod.return?.type ) : [ "any" ]

		tokenizedTypes.each { returnType ->

			// Return type conversions
			if( returnType == "undefined" ) returnType = "void"
			def methodParamResults = iterateMethodParameters( thisMethod )

			def paramNames = methodParamResults.paramNames
			def paramTypes = methodParamResults.paramTypes
			def rawParamTypes = methodParamResults.rawParamTypes
			def requiresOverrides = methodParamResults.requiresOverrides

			def usedPermutations = [:]
			def methodWritten = false

			if( hasOnlyOneSignature( paramNames ) ) {
				writeMethod( thisMethod.shortDoc, thisMethod.name, optionalFlag, paramNames, paramTypes, rawParamTypes, returnType, isInterface, shouldUseExport, isSingleton )
			}
			else if( shouldCreateOverrideMethod( requiresOverrides, tokenizedTypes, returnType ) ) {
				def overrideTypes = []
				paramNames.each { thisParamName ->
					overrideTypes.add( "any" )
				}
				writeMethod( thisMethod.shortDoc, thisMethod.name, optionalFlag, paramNames, overrideTypes, rawParamTypes, "any", isInterface, shouldUseExport, isSingleton )
				usedPermutations[ overrideTypes.join( ',' ) ] = true
				methodWritten = true
			}

			if( config.useFullTyping ) {
				processSignaturePermutations( thisMethod, returnType, paramNames, paramTypes, rawParamTypes, requiresOverrides, usedPermutations, methodWritten )
			}
		}
	}

	def iterateMethodParameters( thisMethod ) {
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

		return [ paramNames: paramNames, paramTypes: paramTypes, rawParamTypes: rawParamTypes, requiresOverrides: requiresOverrides ]
	}

	def processSignaturePermutations( thisMethod, thisType, paramNames, paramTypes, rawParamTypes, requiresOverrides, usedPermutations, methodWritten ) {
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

	def writeMethod( comment, methodName, optionalFlag, paramNames, paramTypes, rawParamTypes, returnType, isInterface, useExport, isStatic=false, omitComment=false ) {
		def paramsContainSpread = hasParametersWithSpread( paramTypes )
		def exportString = useExport ? "export function " : ""
		def staticString = isStaticMethod( isStatic, useExport, methodName ) ? "static " : ""

		comment = "\t\t/** [Method] ${ comment?.replaceAll( "[\\W]", " " ) }"
		def paramsDoc = ""

		if( shouldWriteMethod( useExport, isStatic ) ) {

			if( config.interfaceOnly )
				returnType = typeManager.convertToInterface( returnType )

			def methodOutput = "${ staticString }${ methodName }${ optionalFlag }("
			def paramResult = appendMethodParamOutput( methodOutput, paramsDoc, paramNames, paramTypes, rawParamTypes, paramsContainSpread )
			writeMethodComment( comment, paramResult.paramsDoc, omitComment )
			writeMethodDefinition( paramResult.methodOutput, exportString, methodName, returnType )
		}
	}

	def appendMethodParamOutput( methodOutput, paramsDoc, paramNames, paramTypes, rawParamTypes, paramsContainSpread ) {
		paramNames.eachWithIndex { thisParam, i ->
			def thisParamType = typeManager.convertToInterface( paramTypes[ i ] )
			def thisParamName = thisParam.name

			paramsDoc += "\t\t* @param ${ thisParamName } ${ rawParamTypes[ thisParamName ] } ${ definitionWriter.formatCommentText( thisParam.doc ) }"
			if( thisParam.doc && thisParam.doc.contains( "Optional " ) ) thisParam.optional = true

			def spread = ""
			if( isParamWithSpread( paramNames, thisParam, thisParamType ) ) {
				spread = "..."
			}

			// class is a reserved word in TypeScript
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

		return [ methodOutput: methodOutput, paramsDoc: paramsDoc ]
	}

	def writeMethodComment( comment, paramsDoc, omitComment ) {
		if( shouldIncludeComment( omitComment ) ) {
			if( paramsDoc.length() > 0 ) {
				definitionWriter.writeToDefinition( comment )
				definitionWriter.writeToDefinition( paramsDoc )
				definitionWriter.writeToDefinition( "\t\t*/" )
			}
			else {
				definitionWriter.writeToDefinition( "${ comment }*/" )
			}
		}
	}

	def writeMethodDefinition( methodOutput, exportString, methodName, returnType ) {
		if( methodName != "constructor" ) {
			definitionWriter.writeToDefinition( "\t\t${exportString}${ methodOutput }): ${ typeManager.normalizeType( returnType ) };" )
		}
		else {
			definitionWriter.writeToDefinition( "\t\t${ methodOutput });" )
		}
	}

	def shouldIncludeMethod( fileJson, thisMethod ) {
		def result = false

		if( typeManager.isOwner( fileJson, thisMethod?.owner ) || ( fileJson.mixins && thisMethod.owner in fileJson.mixins ) || isSingleton ) {
			if( ( !isInterface && ( !isSingleton || ( isSingleton && thisMethod.name != "constructor" ) ) ) || ( isInterface && thisMethod.name != "constructor" ) ) {
				if( ( !config.includePrivate && thisMethod.private != true ) || thisMethod.meta?.protected || thisMethod.meta?.template ) {
					if( !processedNames[ thisMethod.name ] && !thisMethod?.meta?.deprecated && !specialCases.shouldRemoveMethod( fileJson.name, thisMethod.name ) ) {
						result = true
					}
				}
			}
		}

		return result
	}

	def normalizeMethodDoc( thisMethod ) {
		if( !thisMethod.shortDoc && thisMethod.short_doc )
			thisMethod.shortDoc = thisMethod.short_doc
	}

	def normalizeReturnType( thisMethod, fileJson ) {
		// Convert return types for special cases where original return type isn't valid
		if( specialCases.getReturnTypeOverride( thisMethod.return?.type ) ) {
			thisMethod.return.type = specialCases.getReturnTypeOverride( thisMethod.return.type )
		}

		// Convert return types for special cases where an overridden subclass method returns an invalid type
		if( thisMethod.return && specialCases.getReturnTypeOverride( fileJson.name, thisMethod.name ) ) {
			thisMethod.return.type = specialCases.getReturnTypeOverride( fileJson.name, thisMethod.name )
		}
	}

	def writeMethodAsProperty( thisMethod ) {
		definitionWriter.writeToDefinition( "\t\t/** [Method] ${ definitionWriter.formatCommentText( thisMethod.shortDoc ) } */" )
		definitionWriter.writeToDefinition( "\t\t${ thisMethod.name.replaceAll( '-', '' ) }${ optionalFlag }: any;" )
	}

	def hasOnlyOneSignature( paramNames ) {
		return !config.useFullTyping || !paramNames.size()
	}

	def shouldCreateOverrideMethod( requiresOverrides, tokenizedReturnTypes, returnType ) {
		return config.useFullTyping && requiresOverrides && tokenizedReturnTypes.first() == returnType
	}

	def hasParametersWithSpread( paramTypes ) {
		return paramTypes.count{ ( it.startsWith( "..." ) || it.endsWith( "..." ) ) } > 0
	}

	def isStaticMethod( isStatic, useExport, methodName ) {
		return isStatic && !useExport && methodName != "constructor"
	}

	def shouldWriteMethod( useExport, isStatic ) {
		return !config.interfaceOnly || ( config.interfaceOnly && ( isInterface || useExport || isStatic ) )
	}

	def shouldIncludeComment( omitComment ) {
		return !config.omitOverrideComments || ( config.omitOverrideComments && !omitComment )
	}

	def isParamWithSpread( paramNames, thisParam, thisParamType ) {
		paramNames.last() == thisParam && ( thisParamType.startsWith( "..." ) || thisParamType.endsWith( "..." ) )
	}

}
