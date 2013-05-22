class Config
{

	// Library info
	def libraryName
	def libraryVersion

	// False to omit private methods/properties
	def includePrivate = false

	// True to include full type values for properties, params, and return types.
	// Generates method overrides where necessary
	// False to omit all types and set everything to "any"
	def useFullTyping = false

	// Force all method parameters to optional where possible (basically any time variable arguments/"spreads" aren't used)
	def forceAllParamsToOptional = true

	// False to generate separate definitions for each package
	def singleDefinition = true
	def currentModule = ""

	// Path to save definition into
	def outputPath = "./target"

	// True to only output JSDoc comments once for each method (and not for overrides) to reduce file size.
	def omitOverrideComments = false

}
