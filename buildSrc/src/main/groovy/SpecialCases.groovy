import groovy.json.JsonSlurper

class SpecialCases implements ISpecialCases
{

	def specialCases = [:]

	SpecialCases() {
		createSpecialCases()
	}

	def createSpecialCases() {
		specialCases[ "removeProperty" ] = [:]
		specialCases[ "removeMethod" ] = [:]
		specialCases[ "methodToProperty" ] = [:]
		specialCases[ "globalReturnTypeOverride" ] = [:]
		specialCases[ "returnTypeOverride" ] = [:]
		specialCases[ "convertParamType" ] = [:]
		specialCases[ "forcedInclude" ] = [:]
		specialCases[ "rewriteMethod" ] = [:]

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
		addRemovedMethod( "Ext.Base", "statics" )
		addRemovedMethod( "Ext.dom.Element", "select" )
		addRemovedMethod( "Ext.Component", "getBubbleTarget" )
		addRemovedMethod( "Ext.tip.ToolTip", "hide" )
		addRemovedMethod( "Ext.tip.QuickTip", "hide" )
		addRemovedMethod( "Ext.tip.ToolTip", "show" )
		addRemovedMethod( "Ext.tip.QuickTip", "show" )
		addRemovedMethod( "Ext.XTemplate", "compile" )
		addRemovedMethod( "Ext.data.proxy.Rest", "buildUrl" )
		addRemovedMethod( "Ext.dd.DDTarget", "getDragEl" )
		addRemovedMethod( "Ext.form.field.Base", "getInputId" )
		addRemovedMethod( "Ext.form.Field", "getInputId" )
		addRemovedMethod( "Ext.form.BaseField", "getInputId" )
		addRemovedMethod( "Ext.form.field.Base", "getSubTplMarkup" )
		addRemovedMethod( "Ext.form.Field", "getSubTplMarkup" )
		addRemovedMethod( "Ext.form.BaseField", "getSubTplMarkup" )
		addRemovedMethod( "Ext.form.field.Trigger", "getSubTplMarkup" )
		addRemovedMethod( "Ext.form.TriggerField", "getSubTplMarkup" )
		addRemovedMethod( "Ext.form.TwinTriggerField", "getSubTplMarkup" )
		addRemovedMethod( "Ext.form.Trigger", "getSubTplMarkup" )
		addRemovedMethod( "Ext.form.field.Spinner", "getSubTplMarkup" )
		addRemovedMethod( "Ext.form.Spinner", "getSubTplMarkup" )

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
		addReturnTypeOverride( "Ext.form.field.Base", "getRawValue", "any" )
		addReturnTypeOverride( "Ext.form.field.Field", "getName", "string" )
		addReturnTypeOverride( "Ext.form.field.Checkbox", "getSubmitValue", "any" )
		addReturnTypeOverride( "Ext.form.field.Base", "getSubmitValue", "any" )
		addReturnTypeOverride( "Ext", "setVersion", "any" )
		addReturnTypeOverride( "Ext.Version", "setVersion", "any" )
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

	def addForcedInclude( className, methodName ) {
		if( !specialCases[ "forcedInclude" ][ className ] ) specialCases[ "forcedInclude" ][ className ] = [:]
		specialCases[ "forcedInclude" ][ className ][ methodName ] = true
	}

	def addRewriteMethod( className, methodName, replacementJson ) {
		if( !specialCases[ "rewriteMethod" ][ className ] ) specialCases[ "rewriteMethod" ][ className ] = [:]
		specialCases[ "rewriteMethod" ][ className ][ methodName ] = replacementJson
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

	def shouldForceInclude( className, methodName ) {
		return ( specialCases[ "forcedInclude" ][ className ] && specialCases[ "forcedInclude" ][ className ][ methodName ] )
	}

	def shouldRewriteMethod( className, methodName ) {
		return ( specialCases[ "rewriteMethod" ][ className ] && specialCases[ "rewriteMethod" ][ className ][ methodName ] )
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

	def getRewriteMethod( className, methodName, methodJson ) {
		if( methodName && specialCases[ "rewriteMethod" ][ className ] && specialCases[ "rewriteMethod" ][ className ][ methodName ] )
			return methodJson << specialCases[ "rewriteMethod" ][ className ][ methodName ]

		return null
	}
}
