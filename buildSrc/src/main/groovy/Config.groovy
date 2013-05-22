class Config
{

	// Library info
	def libraryName
	def libraryVersion

	// False to omit private methods/properties
	def includePrivate

	// True to include full type values for properties, params, and return types.
	// Generates method overrides where necessary
	// False to omit all types and set everything to "any"
	def useFullTyping

	// Force all parameter types to "any" where possible
	def forceAllParamsToOptional

	// False to generate separate definitions for each package
	def singleDefinition
	def currentModule = ""

	// Path to save definition into
	def definitionPath = "./definition"

	// True to only output JSDoc comments once for each method (and not for overrides).
	def omitOverrideComments = false

}
