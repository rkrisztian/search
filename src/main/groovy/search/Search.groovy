package search

import groovy.transform.CompileStatic
import search.colors.AnsiColors
import search.conf.ArgumentsParser
import search.conf.Conf
import search.conf.ConfigParser
import search.filefinder.FileFinder
import search.linefinder.LineFinder
import search.linefinder.LinesCollector
import search.log.ILog
import search.log.Log
import search.resultsprinter.ResultsPrinterFactory

/**
 * Main class.
 */
@CompileStatic
class Search {

	protected final Conf conf

	protected final ILog log

	protected final AnsiColors colors

	protected final String[] args

	Search(Conf conf, ILog log, AnsiColors colors, String[] args) {
		this.conf = conf
		this.log = log
		this.colors = colors
		this.args = args
	}

	static void main(String[] args) {
		def conf = new Conf()
		def log = new Log()
		def colors = new AnsiColors(conf.disableColors)

		new Search(conf, log, colors, args)
				.doSearch()
	}

	protected void doSearch() {
		try {
			parseArgs()
			loadConfig()
			addColorsToLogger()
			findFiles()
		}
		catch (SearchError e) {
			log.error e
			System.exit 1
		}
	}

	protected void parseArgs() {
		def argumentsParser = new ArgumentsParser(conf, log)

		if (!argumentsParser.parseArgs(args)) {
			System.exit 1
		}
	}

	protected void loadConfig() {
		def configParser = new ConfigParser(conf, log)

		configParser.parseConfig()
		conf.setDefaults()

		if (conf.debug) {
			log.debug "Configuration:\n$conf"
		}
	}

	protected void addColorsToLogger() {
		if (!conf.disableColors) {
			log.colors = colors
		}
	}

	protected void findFiles() {
		if (conf.debug) {
			if (conf.paths) {
				log.debug '*** Searching in files...'
			}
			else {
				log.debug '*** Searching in standard input...'
			}
		}

		def linesCollector = new LinesCollector(conf.maxMatchedLinesPerFile, conf.maxContextLines,
				Conf.MAX_DISPLAYED_LINE_LENGTH)
		def resultsPrinter = ResultsPrinterFactory.makeResultsPrinter conf, log
		def lineFinder = new LineFinder(conf, linesCollector, resultsPrinter, log)

		resultsPrinter.withResultsPrinter {
			if (conf.paths) {
				FileFinder fileFinder = new FileFinder(conf, log)

				fileFinder.find { foundFile -> lineFinder.findLines foundFile }
			}
			else {
				lineFinder.findLines()
			}
		}
	}

}
