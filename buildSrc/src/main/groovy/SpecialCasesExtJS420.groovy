class SpecialCasesExtJS420 implements ISpecialCases
{
	def specialCases = [:]

	SpecialCasesExtJS420() {
		createSpecialCases()
	}

	def createSpecialCases() {
		specialCases[ "removeProperty" ] = [:]
		specialCases[ "removeMethod" ] = [:]
		specialCases[ "methodToProperty" ] = [:]
		specialCases[ "globalReturnTypeOverride" ] = [:]
		specialCases[ "returnTypeOverride" ] = [:]
		specialCases[ "convertParamType" ] = [:]

		addRemovedProperty( "Ext.grid.column.Action", "isDisabled" )
		addRemovedProperty( "Ext.Component", "draggable" )
		addRemovedProperty( "Ext.ComponentLoader", "renderer" )
		addRemovedProperty( "Ext.data.TreeStore", "fields" )
		addRemovedProperty( "Ext.util.ComponentDragger", "delegate" )

		addRemovedMethod( "Ext.dd.StatusProxy", "hide" )
		addRemovedMethod( "Ext.tip.Tip", "showAt" )
		addRemovedMethod( "Ext.tip.Tip", "showBy" )
		addRemovedMethod( "Ext.tip.ToolTip", "showAt" )
		addRemovedMethod( "Ext.tip.QuickTip", "showAt" )

		addConvertMethodToProperty( "Ext.AbstractComponent", "animate" )
		addConvertMethodToProperty( "Ext.util.Animate", "animate" )
		addConvertMethodToProperty( "Ext.form.field.Date", "safeParse" )

		addGlobalReturnTypeOverride( "Ext.form.field.Field", "any" )
		addGlobalReturnTypeOverride( "Ext.slider.Multi", "any" )
		addGlobalReturnTypeOverride( "Ext.slider.Single", "any" )

		addReturnTypeOverride( "Ext.data.proxy.Rest", "buildUrl", "string" )
		addReturnTypeOverride( "Ext.data.AbstractStore", "load", "void" )
		addReturnTypeOverride( "Ext.dom.AbstractElement", "hide", "Ext.dom.Element" )
		addReturnTypeOverride( "Ext.dom.AbstractElement", "setVisible", "Ext.dom.Element" )
		addReturnTypeOverride( "Ext.dom.AbstractElement", "show", "Ext.dom.Element" )
		addReturnTypeOverride( "Ext.form.field.Text", "setValue", "any" )
	}

	def addRemovedProperty( className, propertyName ) {
		if( !specialCases[ "removeProperty" ][ className ] ) specialCases[ "removeProperty" ][ className ] = [:]
		specialCases[ "removeProperty" ][ className ][ propertyName ] = true
	}

	def addRemovedMethod( className, methodName ) {
		if( !specialCases[ "removeMethod" ][ className ] ) specialCases[ "removeMethod" ][ className ] = [:]
		specialCases[ "removeMethod" ][ className ][ methodName ] = true
	}

	def addConvertMethodToProperty( className, methodName ) {
		if( !specialCases[ "methodToProperty" ][ className ] ) specialCases[ "methodToProperty" ][ className ] = [:]
		specialCases[ "methodToProperty" ][ className ][ methodName ] = true
	}

	def addGlobalReturnTypeOverride( className, newReturnType="any" ) {
		specialCases[ "globalReturnTypeOverride" ][ className ] = newReturnType
	}

	def addReturnTypeOverride( className, methodName, newReturnType="any" ) {
		if( !specialCases[ "returnTypeOverride" ][ className ] ) specialCases[ "returnTypeOverride" ][ className ] = [:]
		specialCases[ "returnTypeOverride" ][ className ][ methodName ] = newReturnType
	}

	def addMethodParameterOverride( className, methodName, parameterName, newType="any" ) {
		if( !specialCases[ "methodParameterOverride" ][ className ] ) specialCases[ "methodParameterOverride" ][ className ] = [:]
		if( !specialCases[ "methodParameterOverride" ][ className ][ methodName ] ) specialCases[ "methodParameterOverride" ][ className ][ methodName ] = [:]
		specialCases[ "methodParameterOverride" ][ className ][ methodName ][ parameterName ] = newType
	}

	def shouldRemoveProperty( className, propertyName ) {
		return ( specialCases[ "removeProperty" ][ className ] && specialCases[ "removeProperty" ][ className ][ propertyName ] )
	}

	def shouldRemoveMethod( className, methodName ) {
		return ( specialCases[ "removeMethod" ][ className ] && specialCases[ "removeMethod" ][ className ][ methodName ] )
	}

	def shouldConvertToProperty( className, methodName ) {
		return ( specialCases[ "methodToProperty" ][ className ] && specialCases[ "methodToProperty" ][ className ][ methodName ] )
	}

	def getReturnTypeOverride( className, methodName=null ) {
		if( methodName && specialCases[ "returnTypeOverride" ][ className ] && specialCases[ "returnTypeOverride" ][ className ][ methodName ] )
			return specialCases[ "returnTypeOverride" ][ className ][ methodName ]
		return specialCases[ "globalReturnTypeOverride" ][ className ]
	}

	def getMethodParameterOverride( className, methodName, parameterName ) {
		if( specialCases[ "methodParameterOverride" ][ className ] && specialCases[ "methodParameterOverride" ][ className ][ methodName ] && specialCases[ "methodParameterOverride" ][ className ][ methodName ][ parameterName ] )
			return specialCases[ "methodParameterOverride" ][ className ][ methodName ][ parameterName ]

		return null
	}
}
