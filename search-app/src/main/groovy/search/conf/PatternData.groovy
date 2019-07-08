package search.conf

import groovy.transform.CompileStatic

import java.util.regex.Pattern

@CompileStatic
class PatternData implements Dumpable {

	Pattern searchPattern

	Pattern colorReplacePattern

	boolean hidePattern

	boolean replace

	String replaceText

	boolean negativeSearch

	@Override
	public int hashCode() {
		final int prime = 31
		prime + (searchPattern ? searchPattern.pattern().hashCode() : 0)
	}

	@Override
	public boolean equals(Object obj) {
		if (this.is(obj)) {
			return true
		}
		if (!obj) {
			return false
		}
		if (!getClass().is(obj.class)) {
			return false
		}
		PatternData other = (PatternData) obj;
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
