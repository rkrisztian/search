package search.conf

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
		assert !argumentsParser.parseArgs()
		assert argumentsParser.showHelp
	}

	@Test
	void argAllFiles() {
		assert argumentsParser.parseArgs('-a')
		assert conf.paths.size() == 2
	}

	@Test
	void argConfigFile() {
		assert argumentsParser.parseArgs('-c', 'some.file')
		assert conf.configFile == 'some.file'
	}

	@Test
	void argExcludePatterns() {
		assert argumentsParser.parseArgs('-s', 'some.file.1', '-s', 'some.file.2')
		assert conf.excludeFilePatterns.size() == 2
		assert conf.excludeFilePatterns[0].pattern() == /some.file.1/
		assert conf.excludeFilePatterns[1].pattern() == /some.file.2/
	}

	@Test
	void argDebugIsStackable() {
		assert argumentsParser.parseArgs('-d', '-d', '-d')
		assert conf.debug == 3
	}

	@Test
	void argAfterHypen_pattern() {
		assert argumentsParser.parseArgs('-', 'abc')
		assert conf.patternData.size() == 1
		assert conf.patternData[0].searchPattern.pattern() == /abc/
	}

	@Test
	void argAfterHypen_noDuplicatePatterns() {
		assert argumentsParser.parseArgs('-', 'abc', 'abc')
		assert conf.patternData.size() == 1
	}

	@Test
	void argAfterHypen_hypenAsPattern() {
		assert argumentsParser.parseArgs('-', '-')
		assert conf.patternData.size() == 1
		assert conf.patternData[0].searchPattern.pattern() == /-/
	}

	@Test
	void argAfterHypenIsPattern() {
		assert argumentsParser.parseArgs('-', '-a')
		assert conf.patternData.size() == 1
		assert !conf.paths
	}

	@Test
	void argHelpStopsParsing() {
		assert !argumentsParser.parseArgs('--help', '-', 'abc')
		assert conf.patternData.size() == 0
	}

	@Test
	void argAfterHypen_patternWithReplaceText() {
		assert argumentsParser.parseArgs('some.file', '-', 'abc', '-r', 'def')
		assert conf.patternData.size() == 1
		assert conf.patternData[0].searchPattern.pattern() == /abc/
		assert conf.patternData[0].replaceText == 'def'
	}

	@Test
	void argAfterHypenPartiallyHidePatterns() {
		assert argumentsParser.parseArgs('-', '-h', 'abc', 'def')
		assert conf.patternData.size() == 2
		assert conf.patternData[0].hidePattern
		assert !conf.patternData[1].hidePattern
	}

	@Test
	void ignoresReplaceTextWithoutPattern() {
		assert argumentsParser.parseArgs('some.file', '-', '-r', 'abc')
		assert conf.patternData.empty
	}

	@Test
	void replaceTextWithoutPatternDoesNotAffectOtherArgs() {
		assert argumentsParser.parseArgs('some.file', '-', '-r', 'abc', 'def')
		assert conf.patternData.size() == 1
		assert !conf.patternData[0].replace
		assert !conf.patternData[0].replaceText
		assert conf.patternData[0].searchPattern.pattern() == /def/
	}
}
