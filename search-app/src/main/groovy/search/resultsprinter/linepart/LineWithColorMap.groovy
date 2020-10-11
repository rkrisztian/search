package search.resultsprinter.linepart

import groovy.transform.ToString
import search.colors.ColorType

@ToString(includeFields = true, includeNames = false, includePackage = false)
class LineWithColorMap {
	String line
	List<ColorType> colorMap
}
