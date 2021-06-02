package search.linefinder

import static LineType.CONTEXT_LINE
import static LineType.FOUND_LINE
import static LineVisibility.HIDE
import static LineVisibility.SHOW
import static java.nio.file.Files.move
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING
import static search.conf.Constants.REPLACE_TMP_FILE_PATH

import groovy.transform.CompileStatic
import search.conf.Conf
import search.conf.PatternData
import search.log.ILog
import search.resultsprinter.IResultsPrinter

import java.util.regex.Pattern

/**
 * Searches for {@link FoundLine}s, storing them in a {@link ILinesCollector}, and performs string replacements where needed.
 */
@CompileStatic
class LineFinder {

	private final Set<PatternData> patternData

	private final List<Pattern> excludeLinePatterns

	private final boolean doReplace

	private final boolean dryRun

	private final ILinesCollector linesCollector

	private final IResultsPrinter resultsPrinter

	private final ILog log

	LineFinder(Conf conf, ILinesCollector linesCollector, IResultsPrinter resultsPrinter, ILog log) {
		this.patternData = conf.patternData
		this.excludeLinePatterns = conf.excludeLinePatterns
		this.doReplace = conf.doReplace
		this.dryRun = conf.dryRun
		this.linesCollector = linesCollector
		this.resultsPrinter = resultsPrinter
		this.log = log
	}

	void findLines(File file = null) {
		def filePath = file ? file.path : '<STDIN>'
		linesCollector.reset()
		boolean allPatternsFound = true

		if (patternData) {
			def foundPatterns = [] as Set<PatternData>

			searchForPatterns file, foundPatterns
			adjustForNegativeSearch foundPatterns

			if (foundPatterns.size() != patternData.size()) {
				allPatternsFound = false
			}

			if (doReplace && !dryRun && allPatternsFound) {
				replaceInFile(file)
			}
		}

		if (allPatternsFound) {
			resultsPrinter.printFoundLines filePath, linesCollector.foundLines
		}
	}

	private void searchForPatterns(File file, Set foundPatterns) {
		if (file) {
			file.eachLine { String line, int lineNr ->
				searchForPatternsOnLine line, lineNr, foundPatterns
			}
		}
		else {
			System.in.eachLine { String line, int lineNr ->
				searchForPatternsOnLine line, lineNr, foundPatterns
			}
		}
	}

	private void searchForPatternsOnLine(String line, int lineNr, Set foundPatterns) {
		def lineType = CONTEXT_LINE
		def lineVisibility = HIDE

		if (!(excludeLinePatterns.any { line =~ it })) {
			patternData.each { patternData ->
				if (line =~ patternData.searchPattern) {
					foundPatterns << patternData

					if (!patternData.negativeSearch) {
						lineType = FOUND_LINE
						lineVisibility = patternData.hidePattern ? lineVisibility : SHOW
					}
				}
			}
		}

		if (lineType == FOUND_LINE) {
			linesCollector.storeFoundLine lineNr, line, lineVisibility
		}
		else {
			linesCollector.storeContextLine line
		}
	}

	private void adjustForNegativeSearch(Set foundPatterns) {
		patternData.each { patternData ->
			if (patternData.negativeSearch) {
				if (patternData in foundPatterns) {
					foundPatterns.remove patternData
				}
				else {
					foundPatterns << patternData
				}
			}
		}
	}

	// TODO 2013-07-29/rkrisztian: Replace is done by re-opening the same file twice at the moment.
	private void replaceInFile(File file) {
		if (!file.canWrite()) {
			log.fatal "File '${file.path}' is not writable."
		}

		def replaceTmpFile = new File(REPLACE_TMP_FILE_PATH)

		replaceTmpFile.withWriter { writer ->
			file.eachLine { line ->
				if (excludeLinePatterns.any { line =~ it }) {
					return
				}

				def replacedLine = line

				patternData.each { patternData ->
					if (patternData.replace && replacedLine =~ patternData.searchPattern) {
						replacedLine = replacedLine.replaceAll patternData.searchPattern, patternData.replaceText
					}
				}

				writer.writeLine replacedLine
			}
		}

		def origFilePath = file.toPath()
		def tmpFilePathObj = replaceTmpFile.toPath()

		move tmpFilePathObj, origFilePath, REPLACE_EXISTING
	}

}
