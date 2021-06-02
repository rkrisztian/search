package search.resultsprinter

import groovy.transform.CompileStatic
import search.linefinder.FoundLine

/**
 * Outputs search results.
 */
@CompileStatic
interface IResultsPrinter {

	void printFoundLines(String filePath, List<FoundLine> foundLines)

	/** Initializes the printer so that when fatal errors occur, the results found so far are still printed. */
	void withResultsPrinter(Closure action)

}
