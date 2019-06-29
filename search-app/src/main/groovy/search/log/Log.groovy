package search.log

import search.colors.AnsiColors
import search.colors.ColorType

class Log implements ILog {

	AnsiColors colors

	void info(message) {
		println message
	}

	void debug(message) {
		println "[DEBUG] ${message}"
	}

	void warn(message) {
		def prefixedMessage = "[WARN] ${message}"
		System.err.println colors ? colors.format(ColorType.WARNING_COLOR, prefixedMessage) : prefixedMessage
	}

	void error(message) {
		def prefixedMessage = "[ERROR] ${message}"
		System.err.println colors ? colors.format(ColorType.ERROR_COLOR, prefixedMessage) : prefixedMessage
	}

	void fatal(message) {
		error message
		System.exit 1
	}

	/** For search results. */
	void rawPrint(message = '') {
		print message
	}

	/** For search results. */
	void rawPrintln(message = '') {
		println message
	}

	/** For search results. */
	void rawPrintf(message = '', args) {
		printf message, args
	}

}
