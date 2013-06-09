class TypeManager
{
	Config config

	def normalizeReturnType( typeName="any", forceFullType=false ) {
		if( typeName.contains( "/" ) ) {
			if( typeName == "Ext.dom.CompositeElementLite/Ext.dom.CompositeElement" ) typeName = "Ext.dom.CompositeElementLite"
			if( typeName == "Date/null" ) typeName = "any"
		}
		if( typeName.contains( "undefined" ) ) typeName = typeName.replaceAll( "undefined", "void" )
		return typeName
	}

	def normalizeType( typeName="any", forceFullType=false ) {
		if( ( !config.useFullTyping && !forceFullType ) || typeName == "any" )
			return "any"

		typeName = typeName.replaceAll( "\\.\\.\\.", "" )

		if( typeName.contains( "Ext." ) || typeName.contains( "ext." ) )
			typeName = "${ getModule( typeName )}.${ getClassName( typeName ) }"

		def capitalizedTypeName = typeName.capitalize()

		// TypeScript gets confused between native Function type and Ext.Function class...
		if( capitalizedTypeName == "Function" ) typeName = "any"
		if( capitalizedTypeName == "Mixed" ) typeName = "any"
		if( capitalizedTypeName == "TextNode" ) typeName = "any"
		if( capitalizedTypeName == "Arguments" ) typeName = "any"
		if( capitalizedTypeName == "Object" ) typeName = "any"
		if( capitalizedTypeName == "String" ) typeName = "string"
		if( capitalizedTypeName == '"SINGLE"' ) typeName = "string"
		if( capitalizedTypeName == "Boolean" ) typeName = "bool"
		if( capitalizedTypeName == "Number" ) typeName = "number"
		if( capitalizedTypeName == "Date" ) typeName = "any"
		if( capitalizedTypeName == "*" ) typeName = "any"
		if( capitalizedTypeName == "Null" ) typeName = "undefined"
		if( capitalizedTypeName == "Htmlelement" ) typeName = "HTMLElement"
		if( capitalizedTypeName == "Ext.data.INodeinterface" ) typeName = "Ext.data.INodeInterface"
		if( capitalizedTypeName == "Ext.dom.ICompositeelementlite" ) typeName = "Ext.dom.ICompositeElementLite"
		if( capitalizedTypeName == "Google.maps.Map" ) typeName = "any"

		if( capitalizedTypeName == "Array" ) typeName = "any[]"
		if( capitalizedTypeName == "String[]" ) typeName = "string[]"
		if( capitalizedTypeName == "Boolean[]" ) typeName = "bool[]"
		if( capitalizedTypeName == "Number[]" ) typeName = "number[]"
		if( capitalizedTypeName == "Date[]" ) typeName = "any"
		if( capitalizedTypeName == "Object[]" ) typeName = "any[]"
		if( capitalizedTypeName == "Htmlelement[]" ) typeName = "HTMLElement[]"

		return typeName
	}

	def convertToInterface( type ) {
		def result = type
		if( type.contains( "Ext." ) || type.contains( "ext." ) ) {
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
			if( thisName == "ext" ) {
				tokenizedName[ i ] = "Ext"
			}
			else if( thisName != "Ext" ) {
				tokenizedName[ i ] = thisName.toLowerCase()
			}
		}
		return tokenizedName.join( "." )
	}

	def getClassName( name ) {
		def tokenizedName = name.tokenize( "." )
		def className = tokenizedName.last()?.capitalize()
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

	def getTokenizedReturnTypes( types ) {
		if( !types ) types = "void"
		types = normalizeReturnType( types )
		return getTokenizedTypes( types )
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
