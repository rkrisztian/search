package search.resultsprinter.linepart

/**
 * Helps coloring matched/replaced parts of a line.
 */
interface LinePartitioner {

	List<LinePart> partition(String line)

}
