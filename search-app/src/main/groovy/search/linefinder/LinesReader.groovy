package search.linefinder

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * Methods for reading a file or input stream line by line, but allowing the read to stop if a condition is true.
 */
@CompileStatic
class LinesReader {

	static void eachLineWhile(File file,
			@ClosureParams(value = SimpleType, options = ['java.lang.String', 'int']) Closure<Boolean> condition) {
		file.withReader {
			eachLineWhile it, condition
		}
	}

	static void eachLineWhile(InputStream stream,
			@ClosureParams(value = SimpleType, options = ['java.lang.String', 'int']) Closure<Boolean> condition) {
		stream.withReader {
			eachLineWhile it, condition
		}
	}

	static void eachLineWhile(Reader reader,
			@ClosureParams(value = SimpleType, options = ['java.lang.String', 'int']) Closure<Boolean> condition) {
		for (def iterator = reader.iterator(), count = 1; iterator.hasNext(); count++) {
			if (!condition(iterator.next(), count)) {
				break
			}
		}
	}

}
