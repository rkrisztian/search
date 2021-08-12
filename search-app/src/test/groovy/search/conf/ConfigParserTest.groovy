package search.conf

import static search.conf.Conf.DEFAULT_MAX_CONTEXT_LINES
import static search.conf.Conf.DEFAULT_TMP_DIR
import static search.conf.ConfigParser.PROPERTY_EXCLUDE_FILE_PATTERNS
import static search.conf.ConfigParser.PROPERTY_MAX_CONTEXT_LINES
import static search.conf.ConfigParser.PROPERTY_PRINT_HTML
import static search.conf.ConfigParser.PROPERTY_TMP_DIR

import search.log.ILog
import search.log.LogMock
import spock.lang.Specification

import java.nio.file.Paths

class ConfigParserTest extends Specification {

	private final Conf conf = new Conf()
	private final ILog log = LogMock.get()
	private final ConfigParser configParser = new ConfigParser(conf, log)

	void 'null configs are ignored'() {
		when:
			configParser.mapConfigObject(null)

		then:
			noExceptionThrown()
	}

	void 'context lines'() {
		given:
			def config = new ConfigSlurper().parse("""
				${PROPERTY_MAX_CONTEXT_LINES} = 3
			""")
			assert DEFAULT_MAX_CONTEXT_LINES != 3

		when:
			configParser.mapConfigObject config

		then:
			conf.maxContextLines == 3
	}

	void 'max context lines has default value'() {
		when:
			conf.setDefaults()

		then:
			conf.maxContextLines != null
			conf.maxContextLines == DEFAULT_MAX_CONTEXT_LINES
	}

	void 'max context lines can be overridden on command line'() {
		given:
			def config = new ConfigSlurper().parse("""
				${PROPERTY_MAX_CONTEXT_LINES} = 3
			""")
			assert DEFAULT_MAX_CONTEXT_LINES != 3
			assert DEFAULT_MAX_CONTEXT_LINES != 5
			conf.maxContextLines = 5

		when:
			configParser.mapConfigObject config
			conf.setDefaults()

		then:
			conf.maxContextLines == 5
	}

	void 'excludes'() {
		given:
			def config = new ConfigSlurper().parse("""
				${PROPERTY_EXCLUDE_FILE_PATTERNS} = [
					/pattern.1/,
					/pattern.2/
				]
			""")

		when:
			configParser.mapConfigObject config

		then:
			conf.excludeFilePatterns.size() == 2
			conf.excludeFilePatterns[0].pattern() == /pattern.1/
			conf.excludeFilePatterns[1].pattern() == /pattern.2/
	}

	void 'print html, enabled'() {
		given:
			def config = new ConfigSlurper().parse("""
				${PROPERTY_PRINT_HTML} = true
			""")

		when:
			configParser.mapConfigObject config

		then:
			conf.printHtml
	}

	void 'tmp dir can be set to non-default'() {
		given:
			def config = new ConfigSlurper().parse("""
				${PROPERTY_TMP_DIR} = '/dummy/tmp/dir'
			""")

		when:
			configParser.mapConfigObject config
			conf.setDefaults()

		then:
			conf.tmpDir == Paths.get('/dummy/tmp/dir')
			conf.tmpDir != DEFAULT_TMP_DIR
	}

}
