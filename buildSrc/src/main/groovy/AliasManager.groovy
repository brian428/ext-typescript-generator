
class AliasManager
{
	def aliases = [:]
	def TypeManager typeManager
	def Config config

	def init() {
		aliases.xtype = [:]
		aliases.layout = [:]
		aliases.ptype = [:]
		aliases.ftype = [:]
		aliases.event = [:]
	}

	def addAliases( className, fileJson ) {
		if( fileJson.aliases ) {
			addXtypes( className, fileJson )
			addPtypes( className, fileJson )
			addFTypes( className, fileJson )
			addLayouts( className, fileJson )
		}
		if( fileJson.members.event ) {
			addEvents( className, fileJson.members.event )
		}
	}

	def addXtypes( className, fileJson ) {
		def xtypes = fileJson.aliases?.widget

		xtypes.each{ thisXtype ->
			aliases.xtype[ thisXtype ] = thisXtype
		}
	}

	def addPtypes( className, fileJson ) {
		def ptypes = fileJson.aliases?.plugin

		ptypes.each{ thisPtype ->
			aliases.ptype[ thisPtype ] = thisPtype
		}
	}

	def addFTypes( className, fileJson ) {
		def ftypes = fileJson.aliases?.feature

		ftypes.each{ thisFtype ->
			aliases.ftype[ thisFtype ] = thisFtype
		}
	}

	def addLayouts( className, fileJson ) {
		if( className.startsWith( "Ext.layout.container" ) ) {
			def layouts = fileJson.aliases?.layout

			layouts.each{ thisLayout ->
				aliases.layout[ thisLayout ] = thisLayout
			}
		}
	}

	def addEvents( className, eventJson ) {
		if( !aliases.event[ className ] ) aliases.event[ className ] = [:]
		def currentClassEvents = aliases.event[ className ]

		eventJson.each{ thisEvent ->
			if( thisEvent ) {
				currentClassEvents[ thisEvent.name ] = [:]
				currentClassEvents[ thisEvent.name ].eventName = thisEvent.name
				currentClassEvents[ thisEvent.name ].doc = thisEvent.shortDoc
				currentClassEvents[ thisEvent.name ].params = []

				thisEvent.params.each{ thisEventParam ->
					def paramData = [:]
					paramData.name = thisEventParam.name
					paramData.type = thisEventParam.type
					paramData.doc = thisEventParam.doc
					paramData.optional = thisEventParam.optional
					currentClassEvents[ thisEvent.name ].params.push( paramData )
				}
			}
		}
	}

}
