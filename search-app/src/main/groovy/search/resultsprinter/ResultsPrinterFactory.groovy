package search.resultsprinter

import groovy.transform.CompileStatic
import search.colors.AnsiColors
import search.colors.HtmlColors
import search.conf.Conf
import search.log.ILog

@CompileStatic
class ResultsPrinterFactory {

	static IResultsPrinter createResultsPrinter(Conf conf, ILog log) {
		if (conf.printHtml) {
			return new HtmlResultsPrinter(log, new HtmlColors(conf.disableColors))
		}

		new ConsoleResultsPrinter(conf.patternData, conf.doReplace, conf.dryRun, log, conf.disableColors,
				new AnsiColors(conf.disableColors))
	}

}
