class SpecialCasesExtJS extends SpecialCases
{

	def createSpecialCases() {
		super.createSpecialCases()
		addRemovedMethod( "Ext.slider.Multi", "setValue" )
	}

}
