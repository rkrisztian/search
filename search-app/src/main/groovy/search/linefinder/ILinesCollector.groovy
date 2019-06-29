package search.linefinder

interface ILinesCollector {

	void reset()
	void storeFoundLine(int lineNr, String line, LineVisibility lineVisibility)
	void storeContextLine(String line)
	List<FoundLine> getFoundLines()
}
