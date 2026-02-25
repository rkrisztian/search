package search.conf

import static search.conf.ConfigParser.PROPERTY_EXCLUDE_FILE_PATTERNS

import search.log.Log
import search.log.LogMock
import spock.lang.Specification

class ExcludePatternsTest extends Specification {

	private Conf conf
	private Log log
	private ArgumentsParser argumentsParser
	private ConfigParser configParser

	void setup() {
		conf = new Conf()
		log = LogMock.get()
		argumentsParser = new ArgumentsParser(conf, log)
		configParser = new ConfigParser(conf, log)
	}

	void 'config excludes preceed argument excludes'() {
		given:
			def config = new ConfigSlurper().parse("""
				${PROPERTY_EXCLUDE_FILE_PATTERNS} = [
					/pattern.1/,
					/pattern.2/
				]
			""")

		when:
			argumentsParser.parseArgs('-s', 'some.file.1', '-s', 'some.file.2')
			configParser.mapConfigObject config

		then:
			conf.excludeFilePatterns*.pattern() == [/pattern.1/, /pattern.2/, /some.file.1/, /some.file.2/]
	}

}
