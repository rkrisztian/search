package search.conf

import spock.lang.Specification

import java.nio.file.Paths

class GlobPatternTest extends Specification {

	void 'glob pattern works'() {
		expect:
			new GlobPattern('*.java').matches Paths.get('Test.java')
	}

}
