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
	ruleset('rulesets/generic.xml') {
		IllegalClassReference {
			name = 'DoNotUseJavaIoFile'
			priority = 2
			classNames = 'File'
			applyToClassNames = '*'
			description = '"java.io.File" is part of a legacy API, use "java.nio.file.Path"'
		}
	}
	ruleset('rulesets/groovyism.xml') {
		// "GroovyAssertions#assertAll" takes multiple closures, where this style does not make sense
		'ClosureAsLastMethodParameter' doNotApplyToFilesMatching: /.*?Test\.groovy$/
	}
	ruleset 'rulesets/imports.xml'
	ruleset('rulesets/junit.xml') {
		// Not Spock friendly
		'JUnitPublicNonTestMethod' enabled: false
	}
	ruleset('rulesets/logging.xml') {
		// TODO: Think of a better way of logging.
		'Println' enabled: false
		'SystemErrPrint' enabled: false
	}
	ruleset('rulesets/naming.xml') {
		// Not Spock friendly
		'MethodName' doNotApplyToFilesMatching: /.*?Test\.groovy$/
	}
	ruleset('rulesets/size.xml') {
		// JaCoCo is used, not Cobertura.
		'CrapMetric' enabled: false
	}
	ruleset('rulesets/unnecessary.xml') {
		// Not Spock friendly (https://github.com/CodeNarc/CodeNarc/issues/329)
		'UnnecessaryBooleanExpression' doNotApplyToFilesMatching: /.*?Test\.groovy$/
	}
	ruleset 'rulesets/unused.xml'
}
