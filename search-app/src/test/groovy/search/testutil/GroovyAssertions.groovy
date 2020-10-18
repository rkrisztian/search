package search.testutil

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.function.Executable

class GroovyAssertions {

	/**
	 * See <a href="https://issues.apache.org/jira/browse/GROOVY-8045">GROOVY-8045</a>.
	 */
	static void assertAll(Closure... executables) {
		Assertions.assertAll executables.collect { it as Executable }
	}

}
