package search.filefinder

import org.junit.jupiter.api.Test

class BinaryFileCheckerIntTest {

	@Test
	void shouldDetectClassFileAsBinary() {
		def classFile = new File(this.class.classLoader.getResource('example.class').toURI())
		assert BinaryFileChecker.checkIfBinary(classFile)
	}

	@Test
	void shouldDetectSourceFileAsText() {
		def sourceFile = new File(this.class.classLoader.getResource('example.groovy').toURI())
		assert !BinaryFileChecker.checkIfBinary(sourceFile)
	}

	@Test
	void shouldDetectCyrillicAsText() {
		def sourceFile = new File(this.class.classLoader.getResource('russian.txt').toURI())
		assert !BinaryFileChecker.checkIfBinary(sourceFile)
	}

	@Test
	void shouldDetectGreekAsText() {
		def sourceFile = new File(this.class.classLoader.getResource('greek.txt').toURI())
		assert !BinaryFileChecker.checkIfBinary(sourceFile)
	}
}
