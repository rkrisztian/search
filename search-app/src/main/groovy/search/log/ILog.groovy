package search.log

import groovy.transform.CompileStatic

@CompileStatic
interface ILog {

	void info(message)

	void debug(message)

	void warn(message)

	void error(message)

	void fatal(message)

	void rawPrint(message)

	void rawPrintln()

	void rawPrintln(message)

	void rawPrintf(String message, args)
}
