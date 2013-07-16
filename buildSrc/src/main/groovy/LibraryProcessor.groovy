import groovy.json.JsonSlurper

class LibraryProcessor
{
	TypeManager typeManager
	Config config
	ModuleProcessor moduleProcessor

	def init() {
		typeManager = new TypeManager( config: config )
		moduleProcessor = new ModuleProcessor( typeManager: typeManager, config: config )
		moduleProcessor.init()
	}

	def iterateFiles( jsonDocFiles ) {
		jsonDocFiles.each { file ->
			def slurper = new JsonSlurper()
			def fileJson = slurper.parseText( file.text )

			// Only handle classes, not JS primitives like String
			if( fileJson.name.contains( "Ext" ) || typeManager.isCustomNamespace( fileJson.name ) ) {
				def aliases = typeManager.getAliases( fileJson )
				println( "Processing ${ fileJson.name }" )
				moduleProcessor.processModule( fileJson.name, fileJson )

				// Also generate definitions for alternate class names, since crazy ExtJS swaps them out all over the docs...
				fileJson.alternateClassNames.each { thisAlias ->

					// Some aliases are absurdly the same as the original, but with different package case, so skip these
					if( thisAlias.toLowerCase() != fileJson.name.toLowerCase() )
						moduleProcessor.processModule( thisAlias, fileJson )
				}
			}
		}
	}
}
