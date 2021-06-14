package search.resultsprinter.testutil

import groovy.transform.CompileStatic
import search.log.ILog

@CompileStatic
class LogMockForConsoleResultsPrinter implements ILog {

	List<StringBuilder> loggedLines = []

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
		if (!loggedLines) {
			loggedLines += new StringBuilder()
		}

		loggedLines[-1] << message as String
	}

	@Override
	void rawPrintln(Object message = '') {
		rawPrint message
		loggedLines += new StringBuilder()
	}

	@Override
	void rawPrintf(String message, Object... args) {
		rawPrint sprintf(message, args)
	}

	@Override
	void debugException(Exception e) {
	}

}
