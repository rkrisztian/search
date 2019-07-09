ruleset {
	description 'Rules Sample Groovy Gradle Project'

	ruleset 'rulesets/basic.xml'
	ruleset 'rulesets/braces.xml'
	ruleset 'rulesets/exceptions.xml'
	ruleset 'rulesets/imports.xml'
	ruleset('rulesets/logging.xml') {
		'Println' enabled: false
		'SystemErrPrint' enabled: false
	}
	ruleset 'rulesets/naming.xml'
	ruleset 'rulesets/unnecessary.xml'
	ruleset 'rulesets/unused.xml'
}
