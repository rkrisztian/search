package search.conf

import search.log.Log
import search.log.LogMock
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class IncludeConfigTest extends Specification {

	private final Path testConfigFile = Paths.get this.class.classLoader.getResource('conf1.groovy').toURI()
	private final Log log = LogMock.get()

	void 'included config files can override properties'() {
		given:
			def conf = new Conf(configFile: testConfigFile)
			def configParser = new ConfigParser(conf, log)

		when:
			configParser.parseConfig()

		then:
			conf.maxContextLines == 5
	}

	void 'included config files cannot override command-line arguments'() {
		given:
			def conf = new Conf(configFile: testConfigFile, maxContextLines: 10)
			def configParser = new ConfigParser(conf, log)

		when:
			configParser.parseConfig()

		then:
			conf.maxContextLines == 10
	}

}
