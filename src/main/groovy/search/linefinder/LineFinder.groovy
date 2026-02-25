package search.linefinder

import static LineType.CONTEXT_LINE
import static LineType.FOUND_LINE
import static LineVisibility.HIDE
import static LineVisibility.SHOW
import static java.nio.file.Files.delete
import static java.nio.file.Files.isWritable
import static search.conf.Constants.REPLACE_TMP_FILE_NAME
import static search.linefinder.LinesReader.eachLineWhile

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import search.SearchError
import search.annotations.VisibleForTesting
import search.conf.Conf
import search.conf.PatternData
import search.log.Log
import search.resultsprinter.ResultsPrinter

import java.nio.file.Path
import java.util.regex.Pattern

/**
 * Searches for {@link FoundLine}s, storing them in a {@link LinesCollector}, and performs string replacements where needed.
 */
@CompileStatic
class LineFinder {

	private final Set<PatternData> patternData

	private final List<Pattern> excludeLinePatterns

	private final boolean doReplace

	private final boolean dryRun

	private final LinesCollector linesCollector

	private final ResultsPrinter resultsPrinter

	private final Log log

	@PackageScope
	@VisibleForTesting
	final Path replaceTmpFilePath

	LineFinder(Conf conf, LinesCollector linesCollector, ResultsPrinter resultsPrinter, Log log) {
		this.patternData = conf.patternData
		this.excludeLinePatterns = conf.excludeLinePatterns
		this.doReplace = conf.doReplace
		this.dryRun = conf.dryRun
		this.linesCollector = linesCollector
		this.resultsPrinter = resultsPrinter
		this.log = log
		this.replaceTmpFilePath = conf.tmpDir.resolve REPLACE_TMP_FILE_NAME
	}

	void findLines(Path file = null) {
		def filePath = file ? file as String : '<STDIN>'
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

	private void searchForPatterns(Path file, Set foundPatterns) {
		if (file) {
			eachLineWhile(file) { String line, int lineNr ->
				searchForPatternsOnLine line, lineNr, foundPatterns
			}
		}
		else {
			eachLineWhile(System.in) { String line, int lineNr ->
				searchForPatternsOnLine line, lineNr, foundPatterns
			}
		}
	}

	private boolean searchForPatternsOnLine(String line, int lineNr, Set foundPatterns) {
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

		!linesCollector.hasFinished()
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
	private void replaceInFile(Path file) {
		if (!isWritable(file)) {
			throw new SearchError("File '${file}' is not writable.")
		}

		replaceTmpFilePath.withWriter { writer ->
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

				// TODO: Get rid of the cast (GROOVY-10189)
				(writer as BufferedWriter).writeLine replacedLine
			}
		}

		movePreservingOriginalPermissions replaceTmpFilePath, file
	}

	private static void movePreservingOriginalPermissions(Path fromFile, Path toFile) {
		toFile.withOutputStream { os ->
			fromFile.withInputStream { is ->
				os << is
			}
		}

		delete fromFile
	}

}
