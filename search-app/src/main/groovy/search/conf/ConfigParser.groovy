package search.conf

import static java.nio.file.Files.isRegularFile

import groovy.transform.CompileStatic
import search.log.ILog

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Parses configuration files.
 */
@CompileStatic
class ConfigParser {

	protected static final String PROPERTY_EXCLUDE_FILE_PATTERNS = 'excludeFilePatterns'

	protected static final String PROPERTY_MAX_CONTEXT_LINES = 'maxContextLines'

	protected static final String PROPERTY_INCLUDE_CONFIG = 'includeConfig'

	protected static final String PROPERTY_PRINT_HTML = 'printHtml'

	protected static final String PROPERTY_TMP_DIR = 'tmpDir'

	protected final Conf conf

	protected final ILog log

	ConfigParser(Conf conf, ILog log) {
		this.conf = conf
		this.log = log
	}

	void parseConfig(Path file) {
		def newConfig = readConfig file
		parseConfigObject newConfig
	}

	protected ConfigObject readConfig(Path file) {
		if (conf.debug) {
			log.debug "*** Reading configuration file '${file}'..."
		}

		if (!isRegularFile(file)) {
			if (conf.debug) {
				log.debug "Configuration file '${file}' not found."
			}
			return null
		}

		new ConfigSlurper().parse file.toUri().toURL()
	}

	protected void parseConfigObject(ConfigObject newConfig) {
		def stack = [] as Stack

		stack.push newConfig

		while (stack) {
			def currentConfig = stack.pop()

			if (!currentConfig) {
				continue
			}

			if (currentConfig[PROPERTY_EXCLUDE_FILE_PATTERNS]) {
				conf.excludeFilePatterns.addAll 0,
						currentConfig[PROPERTY_EXCLUDE_FILE_PATTERNS].collect { String it -> ~it }
			}
			if (currentConfig[PROPERTY_MAX_CONTEXT_LINES] && (conf.maxContextLines == null)) {
				conf.maxContextLines = currentConfig[PROPERTY_MAX_CONTEXT_LINES] as Integer
			}
			if (currentConfig[PROPERTY_PRINT_HTML]) {
				conf.printHtml = true
			}
			if (currentConfig[PROPERTY_INCLUDE_CONFIG]) {
				currentConfig[PROPERTY_INCLUDE_CONFIG].collect { String it ->
					readConfig Paths.get(it)
				}.each { stack.push it }
			}
			if (currentConfig[PROPERTY_TMP_DIR]) {
				conf.tmpDir = Paths.get currentConfig[PROPERTY_TMP_DIR] as String
			}
		}
	}

}
