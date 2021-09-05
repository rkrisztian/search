package search

import search.colors.AnsiColors
import search.conf.Conf
import search.testutil.LogMockForSmokeTesting
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class SearchTest extends Specification {

	private final Path smokeConfig = Paths.get(this.class.classLoader.getResource('confForSmokeTest.groovy').toURI())

	private final Conf conf = new Conf()

	private final LogMockForSmokeTesting log = new LogMockForSmokeTesting()

	void 'can run simple search'() {
		given:
			def search = new Search(
					conf,
					log,
					new AnsiColors(conf.disableColors),
					['-c', smokeConfig as String, '-C', '-i', '-t', '0', '-l', '1', 'example.groovy', '-', 'RED'] as String[])

		when:
			search.doSearch()

		then:
			verifyAll {
				verifyAll(log.loggedLines.findAll { it }) {
					it.size() == 2
					it[0] =~ /example\.groovy/
					it[1] =~ /RED/
				}
				!log.errorLines
				!log.warnLines
			}
	}

	void 'debug logging works'() {
		given:
			def search = new Search(
					conf,
					log,
					new AnsiColors(conf.disableColors),
					['-c', smokeConfig as String, '-d', 'example.groovy'] as String[])

		when:
			search.doSearch()

		then:
			verifyAll {
				log.debugLines.find { it =~ /^Configuration:/ }
				log.loggedLines.find { it =~ /example\.groovy/ }
				!log.errorLines
				!log.warnLines
			}
	}

}
