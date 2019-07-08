package search.resultsprinter

import groovy.transform.CompileStatic
import search.linefinder.FoundLine

@CompileStatic
interface IResultsPrinter {

	void printFoundLines(String filePath, List<FoundLine> foundLines)

	void withResultsPrinter(Closure action)
}
