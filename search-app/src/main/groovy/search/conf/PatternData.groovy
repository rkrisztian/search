package search.conf

import groovy.transform.CompileStatic
import groovy.transform.ToString

import java.util.regex.Pattern

@CompileStatic
@ToString(includeFields = true, includePackage = false, includeNames = true)
class PatternData {

	Pattern searchPattern

	boolean hidePattern

	boolean replace

	String replaceText

	boolean negativeSearch

	@Override
	int hashCode() {
		final int PRIME = 31
		PRIME + (searchPattern ? searchPattern.pattern().hashCode() : 0)
	}

	@Override
	boolean equals(Object obj) {
		if (this.is(obj)) {
			return true
		}
		if (!obj) {
			return false
		}
		if (!getClass().is(obj.class)) {
			return false
		}
		def other = obj as PatternData
		if (!searchPattern) {
			if (other.searchPattern) {
				return false
			}
		}
		else if (searchPattern.pattern() != other.searchPattern.pattern()) {
			return false
		}
		true
	}
}
