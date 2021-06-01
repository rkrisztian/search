package search.resultsprinter

import groovy.transform.CompileStatic
import search.colors.AnsiColors
import search.colors.HtmlColors
import search.conf.Conf
import search.log.ILog
import search.resultsprinter.linepart.LinePartitioner

/**
 * For selecting the right {@link IResultsPrinter}.
 */
@CompileStatic
class ResultsPrinterFactory {

	static IResultsPrinter makeResultsPrinter(Conf conf, ILog log) {
		def partitioner = new LinePartitioner(conf.patternData, conf.doReplace, conf.dryRun, conf.disableColors)

		if (conf.printHtml) {
			return new HtmlResultsPrinter(conf.patternData, log, new HtmlColors(conf.disableColors), partitioner)
		}

		new ConsoleResultsPrinter(conf.patternData, log, conf.disableColors, new AnsiColors(conf.disableColors), partitioner)
	}

}
