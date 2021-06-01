ruleset {
	description 'Rules Sample Groovy Gradle Project'

	ruleset 'rulesets/basic.xml'
	ruleset 'rulesets/braces.xml'
	ruleset('rulesets/comments.xml') {
		'ClassJavadoc' {
			doNotApplyToFilesMatching = /.*?(Test(Constants)?|Mock.*?|Assertions)\.groovy$/
		}
	}
	ruleset 'rulesets/concurrency.xml'
	ruleset('rulesets/convention.xml') {
		'CompileStatic' {
			doNotApplyToFilesMatching = /.*?(Test(Constants)?|Mock.*?|Assertions)\.groovy$/
		}
		// Tabs are more convenient to use, only mid-line tabs (vertical alignment) should be forbidden.
		'NoTabCharacter' enabled: false
		// Inferred types are actually useful.
		'NoDef' enabled: false
		'ImplicitClosureParameter' enabled: false
		'VariableTypeRequired' enabled: false
		// Implicit returns are useful.
		'ImplicitReturnStatement' enabled: false
		// The newspaper metaphor is the better ordering.
		'StaticMethodsBeforeInstanceMethods' enabled: false
		'PublicMethodsBeforeNonPublicMethods' enabled: false
		// It's the job of the IDEs to make it easier to manage list/map elements.
		'TrailingComma' enabled: false
	}
	ruleset('rulesets/design.xml') {
		// Public fields should be okay for data classes.
		'PublicInstanceField' enabled: false
	}
	ruleset 'rulesets/exceptions.xml'
	ruleset 'rulesets/imports.xml'
	ruleset('rulesets/logging.xml') {
		// TODO: Think of a better way of logging.
		'Println' enabled: false
		'SystemErrPrint' enabled: false
	}
	ruleset 'rulesets/naming.xml'
	ruleset 'rulesets/unnecessary.xml'
	ruleset 'rulesets/unused.xml'
}
