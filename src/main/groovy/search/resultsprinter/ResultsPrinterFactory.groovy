package search.resultsprinter

import groovy.transform.CompileStatic
import search.colors.AnsiColors
import search.colors.HtmlColors
import search.conf.Conf
import search.log.Log
import search.resultsprinter.linepart.LinePartitionerImpl

/**
 * For selecting the right {@link ResultsPrinter}.
 */
@CompileStatic
class ResultsPrinterFactory {

	static ResultsPrinter makeResultsPrinter(Conf conf, Log log) {
		def partitioner = new LinePartitionerImpl(conf.patternData, conf.doReplace, conf.dryRun, conf.disableColors)

		if (conf.printHtml) {
			return new HtmlResultsPrinterImpl(conf.patternData, log, new HtmlColors(conf.disableColors), partitioner, conf.tmpDir)
		}

		new ConsoleResultsPrinter(conf.patternData, log, new AnsiColors(conf.disableColors), partitioner, conf.disableColors)
	}

}
