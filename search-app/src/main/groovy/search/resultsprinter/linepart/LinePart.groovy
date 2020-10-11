package search.resultsprinter.linepart

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import search.colors.ColorType

/**
 * Representation of a line part with color highlighting (when colors are enabled).
 */
@CompileStatic
@EqualsAndHashCode(includeFields = true)
@ToString(includeFields = true, includePackage = false)
class LinePart {
	String text
	ColorType colorType
}
