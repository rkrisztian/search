package search.linefinder

import groovy.transform.CompileStatic

/**
 * Stores {@link FoundLine}s for displaying search results later.
 */
@CompileStatic
interface ILinesCollector {

	void reset()

	void storeFoundLine(int lineNr, String line, LineVisibility lineVisibility)

	void storeContextLine(String line)

	List<FoundLine> getFoundLines()

	boolean hasFinished()

}
