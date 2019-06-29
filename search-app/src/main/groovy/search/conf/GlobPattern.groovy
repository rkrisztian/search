package search.conf

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher

class GlobPattern implements PathMatcher {

	protected final PathMatcher pathMatcher
	protected final String globPattern

	GlobPattern(String globPattern) {
		this.globPattern = globPattern
		pathMatcher = toPathMatcher globPattern
	}

	protected static PathMatcher toPathMatcher(String globPattern) {
		FileSystems.getDefault().getPathMatcher 'glob:' + globPattern
	}

	@Override
	boolean matches(Path path) {
		pathMatcher.matches path
	}

	@Override
	String toString() {
		return globPattern
	}
}
