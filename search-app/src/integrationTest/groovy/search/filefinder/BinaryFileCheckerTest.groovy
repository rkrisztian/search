package search.filefinder

import spock.lang.Specification

import java.nio.file.Paths

class BinaryFileCheckerTest extends Specification {

	void 'should detect file as binary or text'() {
		given:
			def exampleFile = Paths.get this.class.classLoader.getResource(fileName).toURI()

		expect:
			BinaryFileChecker.checkIfBinary(exampleFile) == expectBinary

		where:
			fileName         || expectBinary
			'example.class'  || true
			'example.groovy' || false
			'russian.txt'    || false
			'greek.txt'      || false
	}

}
