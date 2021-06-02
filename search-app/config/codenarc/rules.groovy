ruleset {
	description 'Rules Sample Groovy Gradle Project'

	ruleset 'rulesets/basic.xml'
	ruleset 'rulesets/braces.xml'
	ruleset('rulesets/comments.xml') {
		'ClassJavadoc' doNotApplyToFilesMatching: /.*?(Test(Constants)?|Mock.*?|Assertions)\.groovy$/
	}
	ruleset 'rulesets/concurrency.xml'
	ruleset('rulesets/convention.xml') {
		'CompileStatic' doNotApplyToFilesMatching: /.*?(Test(Constants)?|Mock.*?|Assertions)\.groovy$/
		// Tabs are more convenient to use, only mid-line vertical alignment should be forbidden.
		'NoTabCharacter' enabled: false
		// Inferred types are actually useful.
		'NoDef' enabled: false
		'VariableTypeRequired' enabled: false
		'ImplicitClosureParameter' enabled: false
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
	// TODO: Consider the DRY rules.
	//ruleset 'rulesets/dry.xml'
	ruleset 'rulesets/exceptions.xml'
	ruleset('rulesets/formatting.xml') {
		'LineLength' length: 130
		'Indentation' enabled: false
		'SpaceAroundMapEntryColon' characterAfterColonRegex: /\s/
	}
	ruleset 'rulesets/generic.xml'
	ruleset 'rulesets/groovyism.xml'
	ruleset 'rulesets/imports.xml'
	ruleset('rulesets/junit.xml') {
		// ParameterizedTest should be ignored too (see https://github.com/CodeNarc/CodeNarc/issues/624)
		'JUnitPublicNonTestMethod' ignoreMethodsWithAnnotations: [
				'After', 'AfterAll', 'AfterClass', 'AfterEach', 'Before', 'BeforeAll', 'BeforeClass', 'BeforeEach', 'Disabled',
				'Ignore', 'Override', 'Test', 'ParameterizedTest'
		].join(',')
	}
	ruleset('rulesets/logging.xml') {
		// TODO: Think of a better way of logging.
		'Println' enabled: false
		'SystemErrPrint' enabled: false
	}
	ruleset 'rulesets/naming.xml'
	ruleset('rulesets/size.xml') {
		// JaCoCo is used, not Cobertura.
		'CrapMetric' enabled: false
	}
	ruleset 'rulesets/unnecessary.xml'
	ruleset 'rulesets/unused.xml'
}
