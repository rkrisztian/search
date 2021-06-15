package search.conf

import static search.testutil.GroovyAssertions.assertAll

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import search.log.ILog
import search.log.LogMock

class ArgumentsParserTest {

	private Conf conf
	private ILog log
	private ArgumentsParser argumentsParser

	@BeforeEach
	void setup() {
		conf = new Conf()
		log = LogMock.get()
		argumentsParser = new ArgumentsParser(conf, log)
	}

	@Test
	void showsHelpWithoutArguments() {
		// When
		def success = argumentsParser.parseArgs()

		// Then
		assertAll(
				{ assert !success },
				{ assert argumentsParser.showHelp }
		)
	}

	@Test
	void argAllFiles() {
		// When
		def success = argumentsParser.parseArgs('-a')

		// Then
		assertAll(
				{ assert success },
				{ assert conf.paths.size() == 2 }
		)
	}

	@Test
	void argConfigFile() {
		// When
		def success = argumentsParser.parseArgs('-c', 'some.file')

		// Then
		assertAll(
				{ assert success },
				{ assert conf.configFile == 'some.file' }
		)
	}

	@Test
	void argExcludePatterns() {
		// When
		def success = argumentsParser.parseArgs('-s', 'some.file.1', '-s', 'some.file.2')

		// Then
		assertAll(
				{ assert success },
				{ assert conf.excludeFilePatterns.size() == 2 },
				{ assert conf.excludeFilePatterns[0].pattern() == /some.file.1/ },
				{ assert conf.excludeFilePatterns[1].pattern() == /some.file.2/ }
		)
	}

	@Test
	void argDebugIsStackable() {
		// When
		def success = argumentsParser.parseArgs('-d', '-d', '-d')

		// Then
		assertAll(
				{ assert success },
				{ assert conf.debug == 3 }
		)
	}

	@Test
	void argAfterHypen_pattern() {
		// When
		def success = argumentsParser.parseArgs('-', 'abc')

		// Then
		assertAll(
				{ assert success },
				{ assert conf.patternData.size() == 1 },
				{ assert conf.patternData[0].searchPattern.pattern() == /abc/ }
		)
	}

	@Test
	void argAfterHypen_noDuplicatePatterns() {
		// When
		def success = argumentsParser.parseArgs('-', 'abc', 'abc')

		// Then
		assertAll(
				{ assert success },
				{ assert conf.patternData.size() == 1 }
		)
	}

	@Test
	void argAfterHypen_hypenAsPattern() {
		// When
		def success = argumentsParser.parseArgs('-', '-')

		// Then
		assertAll(
				{ assert success },
				{ assert conf.patternData.size() == 1 },
				{ assert conf.patternData[0].searchPattern.pattern() == /-/ }
		)
	}

	@Test
	void argAfterHypenIsPattern() {
		// When
		def success = argumentsParser.parseArgs('-', '-a')

		// Then
		assertAll(
				{ assert success },
				{ assert conf.patternData.size() == 1 },
				{ assert !conf.paths }
		)
	}

	@Test
	void argHelpStopsParsing() {
		// When
		def success = argumentsParser.parseArgs('--help', '-', 'abc')

		// Then
		assertAll(
				{ assert !success },
				{ assert conf.patternData.size() == 0 }
		)
	}

	@Test
	void argAfterHypen_patternWithReplaceText() {
		// When
		def success = argumentsParser.parseArgs('some.file', '-', 'abc', '-r', 'def')

		// Then
		assertAll(
				{ assert success },
				{ assert conf.patternData.size() == 1 },
				{ assert conf.patternData[0].searchPattern.pattern() == /abc/ },
				{ assert conf.patternData[0].replaceText == 'def' }
		)
	}

	@Test
	void argAfterHypenPartiallyHidePatterns() {
		// When
		def success = argumentsParser.parseArgs('-', '-h', 'abc', 'def')

		// Then
		assertAll(
				{ assert success },
				{ assert conf.patternData.size() == 2 },
				{ assert conf.patternData[0].hidePattern },
				{ assert !conf.patternData[1].hidePattern }
		)
	}

	@Test
	void ignoresReplaceTextWithoutPattern() {
		// When
		def success = argumentsParser.parseArgs('some.file', '-', '-r', 'abc')

		// Then
		assertAll(
				{ assert success },
				{ assert conf.patternData.empty }
		)
	}

	@Test
	void replaceTextWithoutPatternDoesNotAffectOtherArgs() {
		// When
		def success = argumentsParser.parseArgs('some.file', '-', '-r', 'abc', 'def')

		// Then
		assertAll(
				{ assert success },
				{ assert conf.patternData.size() == 1 },
				{ assert !conf.patternData[0].replace },
				{ assert !conf.patternData[0].replaceText },
				{ assert conf.patternData[0].searchPattern.pattern() == /def/ }
		)
	}

	@Test
	void canIgnoreCase() {
		// When
		def success = argumentsParser.parseArgs('some.file', '-i', '-', 'abc')

		// Then
		assertAll(
				{ assert success },
				{ assert 'abc' =~ conf.patternData[0].searchPattern },
				{ assert 'ABC' =~ conf.patternData[0].searchPattern }
		)
	}

	@Test
	void canDoNegativeSearch() {
		// When
		def success = argumentsParser.parseArgs('some.file', '-', '-n', 'abc')

		// Then
		assertAll(
				{ assert success },
				{ assert conf.patternData.size() == 1 },
				{ assert conf.patternData[0].searchPattern.pattern() == /abc/ },
				{ assert conf.patternData[0].negativeSearch }
		)
	}

}
