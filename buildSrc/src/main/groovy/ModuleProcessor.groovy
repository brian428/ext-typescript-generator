class ModuleProcessor
{
	TypeManager typeManager
	Config config
	DefinitionWriter definitionWriter
	ClassProcessor classProcessor

	def init() {
		definitionWriter = new DefinitionWriter( config: config )
		definitionWriter.init()
		classProcessor = new ClassProcessor( typeManager: typeManager, config: config, definitionWriter: definitionWriter )
		classProcessor.init()
	}

	def processModule( className, fileJson ) {
		config.currentModule = typeManager.getModule( className )

		definitionWriter.writeToDefinition( "declare module ${ typeManager.getModule( className ) } {" )
		def processedNames

		// Ext class has special handling to turn it into Ext module-level properties and methods.
		if( className == "Ext" ) {
			processedNames = classProcessor.writeProperties( fileJson, false, true )
			classProcessor.writeMethods( fileJson, processedNames, false, true )
		}
		else {
			classProcessor.processClass( className, fileJson )
		}

		definitionWriter.writeToDefinition( "}" )
	}
}
