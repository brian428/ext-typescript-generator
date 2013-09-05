class SpecialCasesTouch extends SpecialCases
{

	def createSpecialCases() {
		super.createSpecialCases()

		addRemovedProperty( "Ext.Video", "url" )

		addRemovedMethod( "Ext.chart.series.StackedCartesian", "getHidden" )
		addRemovedMethod( "Ext.chart.series.Pie", "getHidden" )
		addRemovedMethod( "Ext.dataview.component.DataItem", "getWidth" )
		addRemovedMethod( "Ext.dataview.component.ListItem", "getItems" )
		addRemovedMethod( "Ext.dataview.component.ListItem", "getTpl" )
		addRemovedMethod( "Ext.dataview.List", "getScrollable" )
		addRemovedMethod( "Ext.dataview.NestedList", "getUi" )
		addRemovedMethod( "Ext.device.purchases.Product", "getUi" )
		addRemovedMethod( "Ext.dom.CompositeElementLite", "first" )
		addRemovedMethod( "Ext.dom.CompositeElementLite", "last" )
		addRemovedMethod( "Ext.field.Slider", "getTabIndex" )
		addRemovedMethod( "Ext.field.Text", "getBubbleEvents" )
		addRemovedMethod( "Ext.picker.Slot", "getItemTpl" )
		addRemovedMethod( "Ext.Sheet", "getHideAnimation" )
		addRemovedMethod( "Ext.Sheet", "getShowAnimation" )
		addRemovedMethod( "Ext.Spacer", "getWidth" )
		addRemovedMethod( "Ext.TitleBar", "getMinHeight" )
		addRemovedMethod( "Ext.Toolbar", "getMinHeight" )
		addRemovedMethod( "Ext.MessageBox", "getHideAnimation" )
		addRemovedMethod( "Ext.MessageBox", "getShowAnimation" )
		addRemovedMethod( "Ext.picker.Picker", "getHeight" )
		addRemovedMethod( "Ext.picker.Picker", "getLeft" )
		addRemovedMethod( "Ext.picker.Picker", "getRight" )

		addRewriteMethod( "Ext.data.Model", "setFields", ["name": "setFields", "params": [["type": "Array","name": "fields"]],"return": ["type": "any"]] )

		addReturnTypeOverride( "Ext.carousel.Carousel", "getIndicator", "any" )
		addReturnTypeOverride( "Ext.chart.series.Polar", "hidden", "any" )
		addReturnTypeOverride( "Ext.Base", "getName", "any" )
		addReturnTypeOverride( "Ext.data.reader.Reader", "getSuccessProperty", "any" )
		addReturnTypeOverride( "Ext.data.reader.Reader", "getTotalProperty", "any" )
		addReturnTypeOverride( "Ext.Component", "getCls", "any" )
		addReturnTypeOverride( "Ext.Component", "getBottom", "any" )
		addReturnTypeOverride( "Ext.mixin.Observable", "getId", "any" )
		addReturnTypeOverride( "Ext.Video", "getUrl", "string" )
		addReturnTypeOverride( "Ext.mixin.Identifiable", "getId", "any" )

	}

}
