class PropertyProcessor
{
	TypeManager typeManager
	Config config
	DefinitionWriter definitionWriter
	ISpecialCases specialCases

	def init() {

	}

	def writeProperties( fileJson, isInterface, useExport ) {
		def classConfig
		def classProperties

		if( fileJson.members instanceof Map ) {
			classConfig = fileJson.members.cfg
			classProperties = fileJson.members.property
		}
		else {
			classConfig = fileJson.members.findAll{ m -> m.tagname == "cfg" }
			classProperties = fileJson.members.findAll{ m -> m.tagname == "property" }
		}

		def optionalFlag = isInterface ? "?" : ""
		def thisType
		def processedConfigNames = [:]
		def exportString = useExport ? "export var " : ""

		if( !useExport && !isInterface && fileJson.singleton ) exportString = "static "

		classConfig.each { value ->
			if( value?.owner == fileJson.name && value.name != "" ) {
				if( !config.includePrivate && value.private != true ) {

					// Don't output special cases where an item should be omitted due to incompatible ExtJS API overrides in subclasses
					if( !specialCases.shouldRemoveProperty( fileJson.name, value.name ) ) {
						thisType = value.type

						if( specialCases.getPropertyTypeOverride( fileJson.name, value.name ) )
							thisType = specialCases.getPropertyTypeOverride( fileJson.name, value.name )

						// Property type conversions
						if( thisType.contains( "/" ) || thisType.contains( "|" ) ) thisType = "any"

						definitionWriter.writeToDefinition( "\t\t/** [Config Option] (${value.type}) ${ definitionWriter.formatCommentText( value.shortDoc ) } */" )
						definitionWriter.writeToDefinition( "\t\t${ exportString }${ value.name.replaceAll( '-', '' ) }${ optionalFlag }: ${ typeManager.normalizeType( typeManager.convertToInterface( thisType ) ) };" )
					}

					processedConfigNames[ value.name ] = true
				}
			}
		}

		if( !isInterface && fileJson.name == "Ext.MessageBox" ) {
			def temp = true
		}

		classProperties.each { value ->
			if( ( !isInterface && fileJson.singleton ) || ( value?.owner == fileJson.name && value.name != "" ) ) {
				if( !config.includePrivate && value.private != true ) {
					if( !processedConfigNames[ value.name ] && !specialCases.shouldRemoveProperty( fileJson.name, value.name ) ) {
						thisType = value.type

						if( specialCases.getPropertyTypeOverride( fileJson.name, value.name ) )
							thisType = specialCases.getPropertyTypeOverride( fileJson.name, value.name )

						if( thisType.contains( "/" ) || thisType.contains( "|" ) ) thisType = "any"

						definitionWriter.writeToDefinition( "\t\t/** [Property] (${value.type}) ${ definitionWriter.formatCommentText( value.shortDoc ) } */" )
						definitionWriter.writeToDefinition( "\t\t${ exportString }${ value.name.replaceAll( '-', '' ) }${ optionalFlag }: ${ typeManager.normalizeType( typeManager.convertToInterface( thisType ) ) };" )
						processedConfigNames[ value.name ] = true
					}
				}
			}
		}
		return processedConfigNames
	}

}
