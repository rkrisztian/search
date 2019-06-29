package search.conf

import static search.conf.Conf.DEFAULT_MAX_CONTEXT_LINES
import static search.conf.ConfigParser.PROPERTY_EXCLUDE_FILE_PATTERNS
import static search.conf.ConfigParser.PROPERTY_MAX_CONTEXT_LINES

import org.junit.Before
import org.junit.Test
import search.log.ILog
import search.log.LogMock

class ConfigParserTest {

	private Conf conf
	private ILog log
	private ConfigParser configParser

	@Before
	void setup() {
		conf = new Conf()
		log = LogMock.get()
		configParser = new ConfigParser(conf, log)
	}

	@Test
	void nullConfigsAreIgnored() {
		configParser.parseConfigObject(null)
	}

	@Test
	void testContextLines() {
		// Given
		def config = new ConfigSlurper().parse("""
			${PROPERTY_MAX_CONTEXT_LINES} = 3
		""")
		assert DEFAULT_MAX_CONTEXT_LINES != 3

		// When
		configParser.parseConfigObject config

		// Then
		assert conf.maxContextLines == 3
	}

	@Test
	void maxContextLinesHasDefaultValue() {
		// When
		conf.setDefaults()

		// Then
		assert conf.maxContextLines != null
		assert conf.maxContextLines == DEFAULT_MAX_CONTEXT_LINES
	}

	@Test
	void maxContextLinesCanBeOverriddenOnCommandLine() {
		// Given
		def config = new ConfigSlurper().parse("""
			${PROPERTY_MAX_CONTEXT_LINES} = 3
		""")
		assert DEFAULT_MAX_CONTEXT_LINES != 3
		assert DEFAULT_MAX_CONTEXT_LINES != 5

		// When
		conf.maxContextLines = 5
		configParser.parseConfigObject config
		conf.setDefaults()

		// Then
		assert conf.maxContextLines == 5
	}

	@Test
	void testExcludes() {
		// Given
		def config = new ConfigSlurper().parse("""
			${PROPERTY_EXCLUDE_FILE_PATTERNS} = [
				/pattern.1/,
				/pattern.2/
			]
		""")

		// When
		configParser.parseConfigObject config

		// Then
		assert conf.excludeFilePatterns.size() == 2
		assert conf.excludeFilePatterns[0].pattern() == /pattern.1/
		assert conf.excludeFilePatterns[1].pattern() == /pattern.2/
	}
}
