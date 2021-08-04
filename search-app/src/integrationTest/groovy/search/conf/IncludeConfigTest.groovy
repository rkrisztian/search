package search.conf

import org.junit.jupiter.api.Test
import search.log.ILog
import search.log.LogMock

import java.nio.file.Path
import java.nio.file.Paths

class IncludeConfigTest {

	@Test
	void includedConfigFilesAreMapped() {
		// Given
		Path testConfigFile = Paths.get this.class.classLoader.getResource('conf1.groovy').toURI()
		Conf conf = new Conf(configFile: testConfigFile)
		ILog log = LogMock.get()
		ConfigParser configParser = new ConfigParser(conf, log)

		// When
		configParser.parseConfig()

		// Then
		assert conf.maxContextLines == 5
	}

}
