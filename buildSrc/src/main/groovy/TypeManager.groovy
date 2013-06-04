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
		if( !types ) types = "void"
		def result = types.replaceAll( "\\|", "/").tokenize( "/" )
		if( result.size() > 1 && types.contains( "Object" ) )
			result = ["any"]
		return result.unique()
	}

	def getExtends( fileJson, isInterface ) {
		def result = ""

		if( fileJson.name == "Ext.Base" ) {
			fileJson.superclasses = [ "Ext.Class" ]
		}
		else if( !fileJson.superclasses && fileJson.extends ) {
			fileJson.superclasses = [ fileJson.extends ]
		}

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
		if( isInterface ) {
			def interfaces = getImplementedInterfaces( fileJson )
			if( !result.length() && interfaces.length() ) {
				result = "extends "
			}
			else if( result.length() && interfaces.length() ) {
				result += ","
			}
			if( interfaces.length() )
				result += interfaces
		}
		return result
	}

	def getImplementedInterfaces( fileJson ) {
		def result = ""

		if( !config.interfaceOnly )
			result = normalizeType( convertToInterface( fileJson.name ), true )

		def implementedInterfaces = []

		fileJson.mixins?.each { thisMixin ->
			implementedInterfaces.add( normalizeType( convertToInterface( thisMixin ), true ) )
		}

		if( implementedInterfaces.size() > 0 ) {
			if( !config.interfaceOnly )
				result += ","
			result += implementedInterfaces.join( ',' )
		}

		return result
	}

	def isOwner( fileJson, candidate ) {
		def result = false

		if( fileJson.name == candidate ) {
			result = true
		}
		else if( fileJson?.alternateClassNames ) {
			fileJson.alternateClassNames.each { thisClassName ->
				if( thisClassName == candidate )
					result = true
			}
		}

		return result
	}


}
