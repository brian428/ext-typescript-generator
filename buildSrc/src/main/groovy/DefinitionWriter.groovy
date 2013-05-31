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

		definitionFile = new File( "${ config.outputPath }/${ config.libraryName }-${ config.libraryVersion }-${ config.useFullTyping ? 'Typed' : 'Untyped' }${ config.interfaceOnly ? '-Interfaces' : '' }.d.ts" )

		if( config.singleDefinition ) {
			definitionFile.write( "" )
			writeToDefinition( "// Type definitions for ${ config.libraryName } ${ config.libraryVersion }" )
			writeToDefinition( "// Project: http://www.sencha.com/products/extjs/" )
			writeToDefinition( "// Definitions by: Brian Kotek <https://github.com/brian428/>" )
			writeToDefinition( "// Definitions: https://github.com/borisyankov/DefinitelyTyped\n" )
		}
	}

	def writeToDefinition( value ) {
		value = value.replaceAll( "(  ){1,100}", " " )
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
