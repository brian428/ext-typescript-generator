class MethodParameters
{

	List paramNames = []
	List paramTypes = []
	Map rawParamTypes = [:]
	Boolean requiresOverrides = false
	TypeManager typeManager
	Config config
	def thisMethod


	def init() {
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
	}

	def cloneWithNewParamTypes( newParamTypes ) {
		MethodParameters methodParameters = new MethodParameters( config: config, typeManager: typeManager, thisMethod: thisMethod )
		methodParameters.paramNames = paramNames
		methodParameters.paramTypes = newParamTypes
		methodParameters.rawParamTypes = rawParamTypes
		methodParameters.requiresOverrides = requiresOverrides
		return methodParameters
	}

	def hasParametersWithSpread() {
		return paramTypes.count{ ( it.startsWith( "..." ) || it.endsWith( "..." ) ) } > 0
	}

	def isParamWithSpread( thisParam, thisParamType ) {
		paramNames.last() == thisParam && ( thisParamType.startsWith( "..." ) || thisParamType.endsWith( "..." ) )
	}

	def hasOnlyOneSignature() {
		return !config.useFullTyping || !paramNames.size()
	}
}
