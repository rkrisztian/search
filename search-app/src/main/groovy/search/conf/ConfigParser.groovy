package search.conf

import groovy.transform.CompileStatic
import search.log.ILog

/**
 * Parses configuration files.
 */
@CompileStatic
class ConfigParser {

	protected static final String PROPERTY_EXCLUDE_FILE_PATTERNS = 'excludeFilePatterns'

	protected static final String PROPERTY_MAX_CONTEXT_LINES = 'maxContextLines'

	protected static final String PROPERTY_INCLUDE_CONFIG = 'includeConfig'

	protected static final String PROPERTY_PRINT_HTML = 'printHtml'

	protected final Conf conf

	protected final ILog log

	ConfigParser(Conf conf, ILog log) {
		this.conf = conf
		this.log = log
	}

	void parseConfig(File file) {
		def newConfig = readConfig file
		parseConfigObject newConfig
	}

	protected ConfigObject readConfig(File file) {
		if (conf.debug) {
			log.debug "*** Reading configuration file '${file}'..."
		}

		if (!file.exists()) {
			if (conf.debug) {
				log.debug "Configuration file '${file}' not found."
			}
			return null
		}

		new ConfigSlurper().parse(file.toURI().toURL())
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
			if (currentConfig[PROPERTY_PRINT_HTML] as Boolean) {
				conf.printHtml = true
			}
			if (currentConfig[PROPERTY_INCLUDE_CONFIG]) {
				currentConfig[PROPERTY_INCLUDE_CONFIG].collect { String it ->
					readConfig new File(it)
				}.each { stack.push it }
			}
		}
	}
}
