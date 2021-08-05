package search.conf

import org.junit.jupiter.api.Test
import search.log.ILog
import search.log.LogMock

import java.nio.file.Path
import java.nio.file.Paths

class IncludeConfigTest {

	private final Path testConfigFile = Paths.get this.class.classLoader.getResource('conf1.groovy').toURI()
	private final ILog log = LogMock.get()

	@Test
	void includedConfigFilesCanOverrideProperties() {
		// Given
		def conf = new Conf(configFile: testConfigFile)
		def configParser = new ConfigParser(conf, log)

		// When
		configParser.parseConfig()

		// Then
		assert conf.maxContextLines == 5
	}

	@Test
	void includedConfigFilesCannotOverrideCommandLineArguments() {
		// Given
		def conf = new Conf(configFile: testConfigFile, maxContextLines: 10)
		def configParser = new ConfigParser(conf, log)

		// When
		configParser.parseConfig()

		// Then
		assert conf.maxContextLines == 10
	}

}
