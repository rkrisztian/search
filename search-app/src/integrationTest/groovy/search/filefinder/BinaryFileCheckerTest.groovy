package search.filefinder

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import java.util.stream.Stream

class BinaryFileCheckerTest {

	@ParameterizedTest
	@MethodSource('shouldDetectFileAsBinaryOrTextArgs')
	void shouldDetectFileAsBinaryOrText(String fileName, boolean expectBinary) {
		def exampleFile = new File(this.class.classLoader.getResource(fileName).toURI())
		assert BinaryFileChecker.checkIfBinary(exampleFile) == expectBinary
	}

	@SuppressWarnings(['unused', 'UnusedPrivateMethod'])
	private static Stream<Arguments> shouldDetectFileAsBinaryOrTextArgs() {
		Stream.of(
				Arguments.of('example.class', true),
				Arguments.of('example.groovy', false),
				Arguments.of('russian.txt', false),
				Arguments.of('greek.txt', false)
		)
	}

}
