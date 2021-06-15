package search.conf

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow
import static search.conf.Conf.DEFAULT_MAX_CONTEXT_LINES
import static search.conf.Conf.DEFAULT_TMP_DIR
import static search.conf.ConfigParser.PROPERTY_EXCLUDE_FILE_PATTERNS
import static search.conf.ConfigParser.PROPERTY_MAX_CONTEXT_LINES
import static search.conf.ConfigParser.PROPERTY_PRINT_HTML
import static search.conf.ConfigParser.PROPERTY_TMP_DIR
import static search.testutil.GroovyAssertions.assertAll

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import search.log.ILog
import search.log.LogMock

class ConfigParserTest {

	private final Conf conf = new Conf()
	private final ILog log = LogMock.get()
	private final ConfigParser configParser = new ConfigParser(conf, log)

	@Test
	void nullConfigsAreIgnored() {
		assertDoesNotThrow({
			configParser.parseConfigObject(null)
		} as Executable)
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

	@Test
	void testPrintHtml_enabled() {
		// Given
		def config = new ConfigSlurper().parse("""
			${PROPERTY_PRINT_HTML} = true
		""")

		// When
		configParser.parseConfigObject config

		// Then
		assert conf.printHtml
	}

	@Test
	void tmpDirCanBeSetToNonDefault() {
		// Given
		def config = new ConfigSlurper().parse("""
			${PROPERTY_TMP_DIR} = '/dummy/tmp/dir'
		""")

		// When
		configParser.parseConfigObject config
		conf.setDefaults()

		// Then
		assertAll(
				{ assert conf.tmpDir == new File('/dummy/tmp/dir') },
				{ assert conf.tmpDir != DEFAULT_TMP_DIR }
		)
	}

}
