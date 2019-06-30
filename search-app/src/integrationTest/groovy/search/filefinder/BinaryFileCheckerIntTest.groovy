package search.filefinder

import org.junit.jupiter.api.Test

class BinaryFileCheckerIntTest {

	@Test
	void shouldDetectClassFileAsBinary() {
		def classFile = new File(this.class.classLoader.getResource('example.class').toURI())
		assert new BinaryFileChecker().checkIfBinary(classFile)
	}

	@Test
	void shouldDetectSourceFileAsText() {
		def sourceFile = new File(this.class.classLoader.getResource('example.groovy').toURI())
		assert !new BinaryFileChecker().checkIfBinary(sourceFile)
	}

}
