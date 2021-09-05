package search.resultsprinter.linepart

/**
 * Helps coloring matched/replaced parts of a line.
 */
interface ILinePartitioner {

	List<LinePart> partition(String line)

}
