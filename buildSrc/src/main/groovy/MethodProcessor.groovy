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

				if( specialCases.shouldRewriteMethod( fileJson.name, thisMethod.name ) )
					thisMethod = specialCases.getRewriteMethod( fileJson.name, thisMethod.name, thisMethod )

				processedNames[ thisMethod.name ] = true
				normalizeMethodDoc( thisMethod )
				handleReturnTypeSpecialCases( thisMethod, fileJson )

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

			def methodParameters = new MethodParameters( config: config, typeManager: typeManager, thisMethod: thisMethod )
			methodParameters.init()

			def usedPermutations = [:]
			def methodWritten = false

			if( methodParameters.hasOnlyOneSignature() ) {
				writeMethod( thisMethod.shortDoc, thisMethod.name, optionalFlag, methodParameters, returnType, shouldUseExport, isSingleton )
			}
			else if( shouldCreateOverrideMethod( methodParameters.requiresOverrides, tokenizedTypes, returnType ) ) {
				def overrideTypes = []
				methodParameters.paramNames.each { thisParamName ->
					overrideTypes.add( "any" )
				}

				def overriddenMethodParams = methodParameters.cloneWithNewParamTypes( overrideTypes )
				writeMethod( thisMethod.shortDoc, thisMethod.name, optionalFlag, overriddenMethodParams, "any", shouldUseExport, isSingleton )
				usedPermutations[ overrideTypes.join( ',' ) ] = true
				methodWritten = true
			}

			if( config.useFullTyping ) {
				processSignaturePermutations( thisMethod, returnType, methodParameters, usedPermutations, methodWritten )
			}
		}
	}

	def processSignaturePermutations( thisMethod, thisType, methodParameters, usedPermutations, methodWritten ) {
		def paramPermutations = GroovyCollections.combinations( methodParameters.paramTypes )

		paramPermutations.each { thisPermutation ->
			if( !methodParameters.requiresOverrides || ( methodParameters.requiresOverrides && thisPermutation.count{ typeManager.normalizeType( it ) == "any" } < thisPermutation.size() ) ) {
				def thisPermutationAsString = thisPermutation.join( ',' )

				if( !usedPermutations[ thisPermutationAsString ] ) {
					def permutationParams = methodParameters.cloneWithNewParamTypes( thisPermutation )
					writeMethod( thisMethod.shortDoc, thisMethod.name, optionalFlag, permutationParams, thisType, shouldUseExport, isSingleton, methodWritten )
					usedPermutations[ thisPermutationAsString ] = true
					methodWritten = true
				}
			}
		}
	}

	def writeMethod( comment, methodName, optionalFlag, methodParameters, returnType, useExport, isStatic=false, omitComment=false ) {
		def exportString = useExport ? "export function " : ""
		def staticString = isStaticMethod( isStatic, useExport, methodName ) ? "static " : ""

		comment = "\t\t/** [Method] ${ comment?.replaceAll( "[\\W]", " " ) }"
		def paramsDoc = ""

		if( shouldWriteMethod( useExport, isStatic ) ) {

			if( config.interfaceOnly )
				returnType = typeManager.convertToInterface( returnType )

			def methodOutput = "${ staticString }${ methodName }${ optionalFlag }("
			def paramResult = appendMethodParamOutput( methodOutput, paramsDoc, methodParameters )
			writeMethodComment( comment, paramResult.paramsDoc, omitComment )
			writeMethodDefinition( paramResult.methodOutput, exportString, methodName, returnType )
		}
	}

	def appendMethodParamOutput( methodOutput, paramsDoc, methodParameters ) {
		methodParameters.paramNames.eachWithIndex { thisParam, i ->
			def thisParamType = typeManager.convertToInterface( methodParameters.paramTypes[ i ] )
			def thisParamName = thisParam.name

			paramsDoc += "\t\t* @param ${ thisParamName } ${ methodParameters.rawParamTypes[ thisParamName ] } ${ definitionWriter.formatCommentText( thisParam.doc ) }"
			if( thisParam.doc && thisParam.doc.contains( "Optional " ) ) thisParam.optional = true

			def spread = ""
			if( methodParameters.isParamWithSpread( thisParam, thisParamType ) ) {
				spread = "..."
			}

			// class is a reserved word in TypeScript
			if( thisParamName == "class" ) thisParamName = "clazz"

			def optionalParamFlag = ( thisParam.optional || spread.size() > 0 ) ? "?" : ""
			if( spread.size() == 0 && config.forceAllParamsToOptional ) {
				optionalParamFlag = "?"
			}
			if( methodParameters.hasParametersWithSpread() ) {
				optionalParamFlag = ""
			}

			methodOutput += " ${spread}${ thisParamName }${ optionalParamFlag }:${ spread.size() > 0 ? "any[]" : typeManager.normalizeType( thisParamType ) }"

			if( thisParam == methodParameters.paramNames.last() ) {
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

	def handleReturnTypeSpecialCases( thisMethod, fileJson ) {
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

	def shouldCreateOverrideMethod( requiresOverrides, tokenizedReturnTypes, returnType ) {
		return config.useFullTyping && requiresOverrides && tokenizedReturnTypes.first() == returnType
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

}
