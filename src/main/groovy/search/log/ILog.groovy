package search.log

import groovy.transform.CompileStatic
import search.colors.AnsiColors

/**
 * A simple logger class suitable for this console app.
 */
@CompileStatic
interface ILog {

	void info(Object message)

	void debug(Object message)

	void warn(Object message)

	void error(Object message)

	void rawPrint(Object message)

	void rawPrintln()

	void rawPrintln(Object message)

	void rawPrintf(String message, Object... args)

	void debugException(Exception e)

	void setColors(AnsiColors colors)

}
