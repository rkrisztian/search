package search.testutil

import search.resultsprinter.testutil.LogMockForConsoleResultsPrinter

class LogMockForSmokeTesting extends LogMockForConsoleResultsPrinter {

	List<String> warnLines = []

	List<String> errorLines = []

	List<String> debugLines = []

	@Override
	void warn(Object message) {
		warnLines += message as String
	}

	@Override
	void error(Object message) {
		errorLines += message as String
	}

	@Override
	void debug(Object message) {
		debugLines += message as String
	}

	@Override
	void debugException(Exception e) {
		def sw = new StringWriter()
		sw.withPrintWriter {
			e.printStackTrace it
		}
		debug sw
	}

}
