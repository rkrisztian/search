package search

import groovy.transform.CompileStatic

/**
 * For known fatal errors we report to the user without stacktrace.
 * (This allows testability without having to mock `System.exit`.)
 */
@CompileStatic
class SearchError extends RuntimeException {

	SearchError(String message) {
		super(message)
	}

}
