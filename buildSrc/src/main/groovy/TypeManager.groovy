class TypeManager
{
	Config config

	def normalizeType( typeName, forceFullType=false ) {
		if( !config.useFullTyping && !forceFullType )
			return "any"

		typeName = typeName.replaceAll( "\\.\\.\\.", "" )

		if( typeName.contains( "Ext." ) )
			typeName = "${ getModule( typeName )}.${ getClassName( typeName ) }"

		// TypeScript gets confused between native Function type and Ext.Function class...
		if( typeName == "Function" ) typeName = "any"
		if( typeName == "Mixed" ) typeName = "any"
		if( typeName == "TextNode" ) typeName = "any"
		if( typeName == "Arguments" ) typeName = "any"
		if( typeName == "Object" ) typeName = "any"
		if( typeName == "String" ) typeName = "string"
		if( typeName == '"SINGLE"' ) typeName = "string"
		if( typeName == "Boolean" ) typeName = "bool"
		if( typeName == "Number" ) typeName = "number"
		if( typeName == "Date" ) typeName = "any"

		if( typeName == "Array" ) typeName = "any[]"
		if( typeName == "String[]" ) typeName = "string[]"
		if( typeName == "Boolean[]" ) typeName = "bool[]"
		if( typeName == "Number[]" ) typeName = "number[]"
		if( typeName == "Date[]" ) typeName = "any"

		return typeName
	}

	def convertToInterface( type ) {
		def result = type
		if( type.contains( "Ext." ) ) {
			result = "${ getModule( type ) }.I${ getClassName( type ) }"
		}
		return result
	}

	def getModule( name ) {
		def tokenizedName = name.tokenize( "." )
		if( tokenizedName.size() == 1 ) return name

		tokenizedName.pop()

		// Some package names absurdly duplicate class names like Ext.data.Store, so lowercase them...
		tokenizedName.eachWithIndex { thisName, i ->
			if( thisName != "Ext" ) tokenizedName[ i ] = thisName.toLowerCase()
		}
		return tokenizedName.join( "." )
	}

	def getClassName( name ) {
		def tokenizedName = name.tokenize( "." )
		def className = tokenizedName.last()
		return className
	}

	def getAliases( fileJson ) {
		return fileJson.aliases.widget
	}

	def getTokenizedTypes( types ) {
		def result = types.replaceAll( "\\|", "/").tokenize( "/" )
		if( result.size() > 1 && types.contains( "Object" ) )
			result = ["any"]
		return result.unique()
	}

	def getExtends( fileJson, isInterface ) {
		// fileJson.superclasses.last() for extends
		def result = ""
		if( fileJson.superclasses?.size() > 0 ) {
			if( isInterface ) {
				if( fileJson.superclasses.last().contains( "Ext." ) )
					result = "extends ${ normalizeType( convertToInterface( fileJson.superclasses.last() ), true ) }"
			}
			else {
				if( getClassName( fileJson.name ) != fileJson.superclasses.last() )
					result = "extends ${ normalizeType( fileJson.superclasses.last(), true ) }"
			}
		}
		return result
	}

	def getImplementedInterfaces( fileJson ) {
		def result = "implements ${ normalizeType( convertToInterface( fileJson.name ), true ) }"
		def implementedInterfaces = []

		fileJson.mixins?.each { thisMixin ->
			implementedInterfaces.add( normalizeType( convertToInterface( thisMixin ), true ) )
		}

		if( implementedInterfaces.size() > 0 ) {
			result += ",${ implementedInterfaces.join( ',' ) }"
		}

		return result
	}


}
