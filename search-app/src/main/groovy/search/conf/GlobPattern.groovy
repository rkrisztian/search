package search.conf

import groovy.transform.CompileStatic

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher

@CompileStatic
class GlobPattern implements PathMatcher {

	protected final PathMatcher pathMatcher
	protected final String globPattern

	GlobPattern(String globPattern) {
		this.globPattern = globPattern
		pathMatcher = toPathMatcher globPattern
	}

	protected static PathMatcher toPathMatcher(String globPattern) {
		FileSystems.default.getPathMatcher 'glob:' + globPattern
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
