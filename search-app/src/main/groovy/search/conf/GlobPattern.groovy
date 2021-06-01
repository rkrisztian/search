package search.conf

import groovy.transform.CompileStatic

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher

/**
 * Represents a glob pattern with helper logic for checking for matches.
 */
@CompileStatic
class GlobPattern implements PathMatcher {

	protected final PathMatcher pathMatcher
	protected final String globPattern

	GlobPattern(String globPattern) {
		this.globPattern = globPattern
		pathMatcher = FileSystems.default.getPathMatcher 'glob:' + globPattern
	}

	@Override
	boolean matches(Path path) {
		pathMatcher.matches path
	}

	@Override
	String toString() {
		globPattern
	}
}
