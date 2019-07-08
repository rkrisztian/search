package search.resultsprinter


import groovy.transform.CompileStatic
import search.colors.AnsiColors
import search.conf.PatternData
import search.linefinder.FoundLine
import search.log.ILog

import static search.colors.ColorType.*
import static search.conf.Constants.SKIPPED_LINES_MARKER

@CompileStatic
class ConsoleResultsPrinter implements IResultsPrinter {

	private final Set<PatternData> patternData

	private final boolean doReplace

	private final boolean dryRun

	private final boolean disableColors

	private final ILog log

	private final AnsiColors colors

	ConsoleResultsPrinter(Set<PatternData> patternData, boolean doReplace, boolean dryRun, ILog log, boolean disableColors,
			AnsiColors colors) {
		this.patternData = patternData
		this.doReplace = doReplace
		this.dryRun = dryRun
		this.log = log
		this.disableColors = disableColors
		this.colors = colors
	}

	void withResultsPrinter(Closure action) {
		if (!disableColors) {
			Runtime.runtime.addShutdownHook {
				// Make sure colors are reset, so the next command prompt is not affected.
				log.rawPrint AnsiColors.RESET
			}
		}

		action()
	}

	void printFoundLines(String filePath, List<FoundLine> foundLines) {
		def filePathLine = filePath + (foundLines ? ' :' : '')
		filePathLine = colors.format FILE_PATH_COLOR, filePathLine

		log.rawPrintln filePathLine

		if (!foundLines) {
			return
		}

		if (!disableColors || doReplace) {
			def replaceColor = dryRun ? DRY_REPLACE_COLOR : REPLACE_COLOR

			foundLines.each { foundLine ->
				patternData.each { patternData ->
					if (patternData.hidePattern) {
						return
					}

					if (!doReplace || !patternData.replace) {
						foundLine.line = foundLine.line.replaceAll patternData.colorReplacePattern,
								colors.format(MATCH_COLOR, '$0')
					}
					else {
						foundLine.line = foundLine.line.replaceAll patternData.colorReplacePattern,
								colors.format(replaceColor, patternData.replaceText)
					}
				}
			}
		}

		boolean prevContextLineAfterOverflow = false

		foundLines.each { foundLine ->
			if (foundLine.contextLinesBefore) {
				printContextLines foundLine.contextLinesBefore, foundLine.contextLinesBeforeOverflow, ContextPosition.BEFORE,
						prevContextLineAfterOverflow
			}

			log.rawPrint '\t'

			if (foundLine.lineNr != -1) {
				def lineNr = sprintf '%6d', foundLine.lineNr
				lineNr = colors.format LINE_NUMBER_COLOR, lineNr

				log.rawPrintln "${lineNr} : ${foundLine.line}"
			}
			else {
				def skippedLinesMarker = sprintf '%6s', SKIPPED_LINES_MARKER
				skippedLinesMarker = colors.format SKIPPED_LINES_MARKER_COLOR, skippedLinesMarker

				log.rawPrintln skippedLinesMarker
			}

			if (foundLine.contextLinesAfter) {
				printContextLines foundLine.contextLinesAfter, foundLine.contextLinesAfterOverflow, ContextPosition.AFTER, false
				prevContextLineAfterOverflow = foundLine.contextLinesAfterOverflow
			}
		}

		log.rawPrintln()
	}

	private void printContextLines(List<String> contextLines, boolean contextLinesOverflow,
			ContextPosition contextPosition, boolean prevContextLineAfterOverflow) {
		if (contextLinesOverflow) {
			def skippedLinesMarker = colors.format CONTEXT_LINES_SKIPPED_LINES_MARKER_COLOR, SKIPPED_LINES_MARKER

			if (contextPosition == ContextPosition.BEFORE) {
				if (!prevContextLineAfterOverflow) {
					contextLines.add 0, skippedLinesMarker
				}
			}
			else {
				contextLines.add skippedLinesMarker
			}
		}

		contextLines.each { contextLine ->
			log.rawPrint '\t'
			log.rawPrintf '%6s', ''
			log.rawPrint '   '
			contextLine = colors.format CONTEXT_LINES_COLOR, contextLine
			log.rawPrintln contextLine
		}
	}

}
