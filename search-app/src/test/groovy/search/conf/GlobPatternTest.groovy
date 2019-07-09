package search.conf

import org.junit.jupiter.api.Test

class GlobPatternTest {

	@Test
	void globPatternWorks() {
		assert new GlobPattern('*.java').matches(new File('Test.java').toPath())
	}
}
