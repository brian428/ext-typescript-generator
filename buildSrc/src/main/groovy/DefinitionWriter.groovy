/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 5/22/13
 * Time: 12:50 AM
 * To change this template use File | Settings | File Templates.
 */
class DefinitionWriter
{
	Config config
	File definitionFile

	def init() {
		new File( config.outputPath ).deleteDir()
		new File( config.outputPath ).mkdir()

		definitionFile = new File( "${ config.outputPath }/${ config.libraryName }-${ config.libraryVersion }-${ config.useFullTyping ? 'Typed' : 'Untyped' }.d.ts" )

		if( config.singleDefinition ) {
			definitionFile.write( "" )
			writeToDefinition( "// ${ config.libraryName } ${ config.libraryVersion } TypeScript Library Definition\n" )
		}
	}

	def writeToDefinition( value ) {
		if( config.singleDefinition ) {
			definitionFile.withWriterAppend( "UTF-8" ) { writer ->
				writer << value + "\n"
			}
		}
		else {
			def currentDefinitionFile = new File( "${ config.outputPath }/${ config.currentModule }.d.ts" )
			if( !currentDefinitionFile.exists() ) currentDefinitionFile.createNewFile()

			currentDefinitionFile.withWriterAppend( "UTF-8" ) { writer ->
				writer << value + "\n"
			}
		}
	}

	def formatCommentText( comment ) {
		def result = ""
		if( comment ) result += comment.replaceAll("<(.|\n)*?>", "").replaceAll( "[\\n\\t]", " " )
		return result
	}
}
