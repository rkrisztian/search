package search.resultsprinter

import search.linefinder.FoundLine

interface IResultsPrinter {

	void printFoundLines(String filePath, List<FoundLine> foundLines)

	void withResultsPrinter(Closure action)
}
