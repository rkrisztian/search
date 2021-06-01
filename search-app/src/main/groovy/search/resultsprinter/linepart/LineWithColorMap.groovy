package search.resultsprinter.linepart

import groovy.transform.CompileStatic
import groovy.transform.ToString
import search.colors.ColorType

/**
 * Stores the color of each character of a line.
 */
@CompileStatic
@ToString(includeFields = true, includeNames = false, includePackage = false, ignoreNulls = true)
class LineWithColorMap {
	String line
	List<ColorType> colorMap
}
