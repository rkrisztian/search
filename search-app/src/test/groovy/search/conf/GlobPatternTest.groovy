package search.conf

import org.junit.jupiter.api.Test

import java.nio.file.Paths

class GlobPatternTest {

	@Test
	void globPatternWorks() {
		assert new GlobPattern('*.java').matches(Paths.get('Test.java'))
	}

}
