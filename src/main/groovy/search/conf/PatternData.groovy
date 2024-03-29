package search.conf

import groovy.transform.CompileStatic
import groovy.transform.ToString

import java.util.regex.Pattern

/**
 * Stores information about a pattern to search for or replace.
 *
 * <p>{@link groovy.transform.EqualsAndHashCode} is not used here, because {@link Pattern} does not have an equals contract.</p>
 */
@CompileStatic
@ToString(includeFields = true, includes = ['searchPattern', 'replaceText', 'hidePattern'],
		includePackage = false, includeNames = true, ignoreNulls = true)
class PatternData {

	Pattern searchPattern

	boolean hidePattern

	boolean replace

	String replaceText

	boolean negativeSearch

	@Override
	int hashCode() {
		final int prime = 31
		prime + (searchPattern ? searchPattern.pattern().hashCode() : 0)
	}

	@Override
	boolean equals(Object obj) {
		if (!getClass().is(obj.class)) {
			return false
		}
		def other = obj as PatternData

		if (!searchPattern || !other.searchPattern) {
			return !searchPattern && !other.searchPattern
		}
		if (searchPattern.pattern() != other.searchPattern.pattern()) {
			return false
		}
		true
	}

}
