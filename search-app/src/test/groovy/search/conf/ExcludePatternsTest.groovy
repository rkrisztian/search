package search.conf

import static search.conf.ConfigParser.PROPERTY_EXCLUDE_FILE_PATTERNS

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import search.log.ILog
import search.log.LogMock

class ExcludePatternsTest {

	private Conf conf
	private ILog log
	private ArgumentsParser argumentsParser
	private ConfigParser configParser

	@BeforeEach
	void setup() {
		conf = new Conf()
		log = LogMock.get()
		argumentsParser = new ArgumentsParser(conf, log)
		configParser = new ConfigParser(conf, log)
	}

	@Test
	void testConfigExcludesPreceedArgumentExcludes() {
		// Given
		def config = new ConfigSlurper().parse("""
			${PROPERTY_EXCLUDE_FILE_PATTERNS} = [
				/pattern.1/,
				/pattern.2/
			]
		""")

		// When
		argumentsParser.parseArgs('-s', 'some.file.1', '-s', 'some.file.2')
		configParser.parseConfigObject config

		// Then
		assert conf.excludeFilePatterns.size() == 4
		assert conf.excludeFilePatterns[0].pattern() == /pattern.1/
		assert conf.excludeFilePatterns[1].pattern() == /pattern.2/
		assert conf.excludeFilePatterns[2].pattern() == /some.file.1/
		assert conf.excludeFilePatterns[3].pattern() == /some.file.2/
	}

}
