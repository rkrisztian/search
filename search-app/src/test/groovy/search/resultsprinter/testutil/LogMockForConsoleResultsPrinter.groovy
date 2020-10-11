package search.resultsprinter.testutil

import search.log.ILog

class LogMockForConsoleResultsPrinter implements ILog {

	List<StringBuffer[]> loggedLines = []

	@Override
	void info(Object message) {
	}

	@Override
	void debug(Object message) {
	}

	@Override
	void warn(Object message) {
	}

	@Override
	void error(Object message) {
	}

	@Override
	void fatal(Object message) {
	}

	@Override
	void rawPrint(Object message) {
		loggedLines.last() << message
	}

	@Override
	void rawPrintln(Object message = '') {
		loggedLines += message
	}

	@Override
	void rawPrintf(String message, Object args) {
		rawPrint sprintf(message, args)
	}

	@Override
	void debugException(Exception e) {
	}
}
