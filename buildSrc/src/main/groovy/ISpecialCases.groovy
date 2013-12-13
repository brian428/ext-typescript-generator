public interface ISpecialCases {

	def createSpecialCases()

	def addRemovedProperty( className, propertyName )

	def addRemovedMethod( className, methodName )

	def addConvertMethodToProperty( className, methodName )

	def addGlobalReturnTypeOverride( className, newReturnType )

	def addReturnTypeOverride( className, methodName, newReturnType )

	def addMethodParameterOverride( className, methodName, parameterName, newType )

	def addForcedInclude( className, methodName )

	def addRewriteMethod( className, methodName, replacementJson )

	def addPropertyTypeOverride( className, propertyName, newType )

	def shouldRemoveProperty( className, propertyName )

	def shouldRemoveMethod( className, methodName )

	def shouldForceInclude( className, methodName )

	def shouldConvertToProperty( className, methodName )

	def shouldRewriteMethod( className, methodName )

	def getReturnTypeOverride( className, methodName )

	def getMethodParameterOverride( className, methodName, parameterName )

	def getRewriteMethod( className, methodName, methodJson )

	def getPropertyTypeOverride( className, propertyName )

}