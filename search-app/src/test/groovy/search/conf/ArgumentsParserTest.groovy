package search.conf

import search.log.ILog
import search.log.LogMock
import spock.lang.Specification

class ArgumentsParserTest extends Specification {

	private Conf conf
	private ILog log
	private ArgumentsParser argumentsParser

	void setup() {
		conf = new Conf()
		log = LogMock.get()
		argumentsParser = new ArgumentsParser(conf, log)
	}

	void 'shows help without arguments'() {
		when:
			def success = argumentsParser.parseArgs()

		then:
			!success
			argumentsParser.showHelp
	}

	void 'arg all files'() {
		when:
			def success = argumentsParser.parseArgs('-a')

		then:
			success
			conf.paths.size() == 2
	}

	void 'arg config file'() {
		when:
			def success = argumentsParser.parseArgs('-c', 'some.file')

		then:
			success
			conf.configFile == 'some.file'
	}

	void 'arg exclude patterns'() {
		when:
			def success = argumentsParser.parseArgs('-s', 'some.file.1', '-s', 'some.file.2')

		then:
			success
			conf.excludeFilePatterns*.pattern() == [/some.file.1/, /some.file.2/]
	}

	void 'arg debug is stackable'() {
		when:
			def success = argumentsParser.parseArgs('-d', '-d', '-d')

		then:
			success
			conf.debug == 3
	}

	void 'arg after hypen, simple pattern'() {
		when:
			def success = argumentsParser.parseArgs('-', 'abc')

		then:
			success
			conf.patternData*.searchPattern*.pattern() == [/abc/]
	}

	void 'arg after hypen, no duplicate patterns'() {
		when:
			def success = argumentsParser.parseArgs('-', 'abc', 'abc')

		then:
			success
			conf.patternData.size() == 1
	}

	void 'arg after hypen, hypen as pattern'() {
		when:
			def success = argumentsParser.parseArgs('-', '-')

		then:
			success
			conf.patternData*.searchPattern*.pattern() == [/-/]
	}

	void 'arg after hypen, arg as pattern'() {
		when:
			def success = argumentsParser.parseArgs('-', '-a')

		then:
			success
			conf.patternData.size() == 1
			!conf.paths
	}

	void 'arg help stops parsing'() {
		when:
			def success = argumentsParser.parseArgs('--help', '-', 'abc')

		then:
			!success
			!conf.patternData
	}

	void 'arg after hypen, pattern with replace text'() {
		when:
			def success = argumentsParser.parseArgs('some.file', '-', 'abc', '-r', 'def')

		then:
			success
			with(conf.patternData) {
				size() == 1
				it[0].searchPattern.pattern() == /abc/
				it[0].replaceText == 'def'
			}
	}

	void 'arg after hypen, partially hide patterns'() {
		when:
			def success = argumentsParser.parseArgs('-', '-h', 'abc', 'def')

		then:
			success
			conf.patternData*.hidePattern == [true, false]
	}

	void 'ignores replace text without pattern'() {
		when:
			def success = argumentsParser.parseArgs('some.file', '-', '-r', 'abc')

		then:
			success
			conf.patternData.empty
	}

	void 'replace text without pattern does not affect other args'() {
		when:
			def success = argumentsParser.parseArgs('some.file', '-', '-r', 'abc', 'def')

		then:
			success
			with(conf.patternData) {
				size() == 1
				!it[0].replace
				!it[0].replaceText
				it[0].searchPattern.pattern() == /def/
			}
	}

	void 'can ignore case'() {
		when:
			def success = argumentsParser.parseArgs('some.file', '-i', '-', 'abc')

		then:
			success
			with(conf.patternData[0]) {
				'abc' =~ searchPattern
				'ABC' =~ searchPattern
			}
	}

	void 'can do negative search'() {
		when:
			def success = argumentsParser.parseArgs('some.file', '-', '-n', 'abc')

		then:
			success
			conf.patternData.size() == 1

			with(conf.patternData[0]) {
				searchPattern.pattern() == /abc/
				negativeSearch
			}
	}

	void 'unknown flags are treated as file patterns'() {
		when:
			def success = argumentsParser.parseArgs('-i', '-Q')

		then:
			success
			conf.paths*.globPattern == ['-Q']
	}

}
