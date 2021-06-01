package search.log

import groovy.transform.CompileStatic
import search.colors.AnsiColors
import search.colors.ColorType

/**
 * A simple logger class suitable for this console app.
 */
@CompileStatic
class Log implements ILog {

	AnsiColors colors

	void info(Object message) {
		println message
	}

	void debug(Object message) {
		println "[DEBUG] ${message}"
	}

	void warn(Object message) {
		def prefixedMessage = "[WARN] ${message}"
		System.err.println colors ? colors.format(ColorType.WARNING_COLOR, prefixedMessage) : prefixedMessage
	}

	void error(Object message) {
		def prefixedMessage = "[ERROR] ${message}"
		System.err.println colors ? colors.format(ColorType.ERROR_COLOR, prefixedMessage) : prefixedMessage
	}

	void fatal(Object message) {
		error message
		System.exit 1
	}

	/** For search results. */
	void rawPrint(Object message = '') {
		print message
	}

	/** For search results. */
	void rawPrintln(Object message = '') {
		println message
	}

	/** For search results. */
	void rawPrintf(String message = '', Object... args) {
		printf message, args
	}

	void debugException(Exception e) {
		def sw = new StringWriter()
		sw.withPrintWriter {
			e.printStackTrace it
		}
		debug sw
	}
}
