package search.log

interface ILog {

	void info(message)
	void debug(message)
	void warn(message)
	void error(message)
	void fatal(message)
	void rawPrint(message)
	void rawPrintln(message)
	void rawPrintf(message, args)
}
