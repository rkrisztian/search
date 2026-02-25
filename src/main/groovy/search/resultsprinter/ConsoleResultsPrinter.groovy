package search.resultsprinter

import static search.colors.ColorType.CONTEXT_LINES_COLOR
import static search.colors.ColorType.CONTEXT_LINES_SKIPPED_LINES_MARKER_COLOR
import static search.colors.ColorType.FILE_PATH_COLOR
import static search.colors.ColorType.LINE_NUMBER_COLOR
import static search.conf.Constants.SKIPPED_LINES_MARKER

import groovy.transform.CompileStatic
import search.colors.AnsiColors
import search.conf.PatternData
import search.linefinder.FoundLine
import search.log.Log
import search.resultsprinter.linepart.LinePartitioner

/**
 * Displays search results in the console output.
 */
@CompileStatic
class ConsoleResultsPrinter implements ResultsPrinter {

	private final Set<PatternData> patternData

	private final Log log

	private final AnsiColors colors

	private final LinePartitioner partitioner

	private final boolean disableColors

	ConsoleResultsPrinter(Set<PatternData> patternData, Log log, AnsiColors colors, LinePartitioner partitioner,
			boolean disableColors) {
		this.patternData = patternData
		this.log = log
		this.colors = colors
		this.partitioner = partitioner
		this.disableColors = disableColors
	}

	@Override
	void withResultsPrinter(Closure action) {
		if (!disableColors) {
			Runtime.runtime.addShutdownHook {
				// Make sure colors are reset, so the next command prompt is not affected.
				log.rawPrint AnsiColors.RESET
			}
		}

		action()
	}

	@Override
	void printFoundLines(String filePath, List<FoundLine> foundLines) {
		def filePathLine = filePath + (foundLines ? ' :' : '')
		filePathLine = colors.format FILE_PATH_COLOR, filePathLine

		log.rawPrintln filePathLine

		if (!foundLines) {
			return
		}

		foundLines.each { foundLine ->
			if (foundLine.contextLinesBefore) {
				printContextLines foundLine.contextLinesBefore, foundLine.contextLinesBeforeOverflow, ContextPosition.BEFORE
			}

			printFoundLine foundLine

			if (foundLine.contextLinesAfter) {
				printContextLines foundLine.contextLinesAfter, foundLine.contextLinesAfterOverflow, ContextPosition.AFTER
			}
		}

		log.rawPrintln()
	}

	private void printFoundLine(FoundLine foundLine) {
		def lineNr = sprintf '%6d', foundLine.lineNr

		log.rawPrint "\t${colors.format LINE_NUMBER_COLOR, lineNr} : "

		partitioner.partition(foundLine.line).each { lp ->
			log.rawPrint((lp.colorType) ? colors.format(lp.colorType, lp.text) : lp.text)
		}

		log.rawPrintln()
	}

	private void printContextLines(List<String> contextLines, boolean contextLinesOverflow, ContextPosition contextPosition) {
		if (contextLinesOverflow) {
			def skippedLinesMarker = colors.format CONTEXT_LINES_SKIPPED_LINES_MARKER_COLOR, SKIPPED_LINES_MARKER

			if (contextPosition == ContextPosition.BEFORE) {
				contextLines.add 0, skippedLinesMarker
			}
			else {
				contextLines.add skippedLinesMarker
			}
		}

		contextLines.each { contextLine ->
			log.rawPrint '\t'
			log.rawPrintf '%6s', ''
			log.rawPrint '   '
			log.rawPrintln colors.format(CONTEXT_LINES_COLOR, contextLine)
		}
	}

}
