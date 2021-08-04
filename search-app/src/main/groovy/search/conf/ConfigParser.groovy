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

	private static final String INTERNAL_PROPERTY_CURRENT_FILE = '__currentFile'

	protected final Conf conf

	protected final ILog log

	ConfigParser(Conf conf, ILog log) {
		this.conf = conf
		this.log = log
	}

	void parseConfig() {
		def newConfig = readConfig Paths.get(conf.configFile)
		mapConfigObject(newConfig)
	}

	private ConfigObject readConfig(Path file) {
		if (conf.debug) {
			log.debug "*** Reading configuration file '${file}'..."
		}

		if (!isRegularFile(file)) {
			if (conf.debug) {
				log.debug "Configuration file '${file}' not found."
			}
			return null
		}

		def newConfig = new ConfigSlurper().parse file.toUri().toURL()
		newConfig[INTERNAL_PROPERTY_CURRENT_FILE] = file as String

		newConfig
	}

	protected void mapConfigObject(ConfigObject newConfig) {
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
				currentConfig[PROPERTY_INCLUDE_CONFIG].each {
					stack.push readConfig(
							Paths.get(currentConfig[INTERNAL_PROPERTY_CURRENT_FILE] as String).parent.resolve(it as String))
				}
			}
			if (currentConfig[PROPERTY_TMP_DIR]) {
				conf.tmpDir = Paths.get currentConfig[PROPERTY_TMP_DIR] as String
			}
		}
	}

}
