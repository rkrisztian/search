package search.resultsprinter

import static search.colors.ColorType.CONTEXT_LINES_COLOR
import static search.colors.ColorType.CONTEXT_LINES_SKIPPED_LINES_MARKER_COLOR
import static search.colors.ColorType.FILE_PATH_COLOR
import static search.colors.ColorType.LINE_NUMBER_COLOR
import static search.colors.ColorType.SKIPPED_LINES_MARKER_COLOR
import static search.conf.Constants.HTML_TMP_FILE_NAME
import static search.conf.Constants.SKIPPED_LINES_MARKER

import groovy.transform.CompileDynamic
import groovy.xml.MarkupBuilder
import search.colors.HtmlColors
import search.conf.PatternData
import search.linefinder.FoundLine
import search.log.ILog
import search.resultsprinter.linepart.ILinePartitioner

/**
 * Saves the search results into an HTML file.
 */
@CompileDynamic
class HtmlResultsPrinter implements IResultsPrinter {

	private final String htmlStyle = '''
			body {
				/* TODO 01-Jul-2018/rkrisztian: Black-on-white for now. Make it configurable. */
				background-color: #000000;
				color: #C0C0C0;
			}
			h4 {
				white-space: pre;
			}
			.code {
				font-family: "Courier New", Courier, monospace;
			}
			.marker {
				font-style: italic;
			}
			td {
				padding: 0 0 0 0;
				margin: 0 0 0 0;
			}
			td.lineNr {
				padding-right: 30px;
				text-align: right;
			}
			pre {
				padding: 0 0 0 0;
				margin: 0 0 0 0;
				display: inline-flex;
			}
	'''.stripIndent()

	private final Set<PatternData> patternData

	private final ILog log

	private final HtmlColors colors

	private final List<String> htmlBodyParts = []

	private final ILinePartitioner partitioner

	private final File tmpDir

	HtmlResultsPrinter(Set<PatternData> patternData, ILog log, HtmlColors colors, ILinePartitioner partitioner, File tmpDir) {
		this.patternData = patternData
		this.log = log
		this.colors = colors
		this.partitioner = partitioner
		this.tmpDir = tmpDir
	}

	@Override
	void withResultsPrinter(Closure action) {
		try {
			action()
		}
		finally {
			def htmlFile = new File(tmpDir, HTML_TMP_FILE_NAME)

			htmlFile.withWriter('utf-8') { writer ->
				new MarkupBuilder(writer).html {
					head {
						meta charset: 'utf-8'
						title 'Search results'
						style type: 'text/css', htmlStyle
					}
					body {
						htmlBodyParts.each {
							mkp.yieldUnescaped it
						}
					}
				}
			}

			openHtmlFile htmlFile
		}
	}

	// See false positive for `MarkupBuilder(...).div {}` (https://github.com/CodeNarc/CodeNarc/issues/623)
	// codenarc-disable ExplicitCallToDivMethod
	@Override
	void printFoundLines(String filePath, List<FoundLine> foundLines) {
		def bodyPartWriter = new StringWriter()
		def builder = new MarkupBuilder(bodyPartWriter)

		builder.div {
			h4([class: 'code'] + colors.format(FILE_PATH_COLOR), filePath)

			if (foundLines) {
				table {
					def prevContextLineAfterOverflow = false

					foundLines.each { foundLine ->
						if (foundLine.contextLinesBefore) {
							printContextLines builder, foundLine.contextLinesBefore,
									foundLine.contextLinesBeforeOverflow, ContextPosition.BEFORE,
									prevContextLineAfterOverflow
						}

						tr {
							if (foundLine.lineNr != -1) {
								td([class: 'lineNr'] + colors.format(LINE_NUMBER_COLOR)) {
									pre([class: 'code'], foundLine.lineNr)
								}
								td {
									colorLine builder, foundLine.line
								}
							}
							else {
								td class: 'lineNr'
								td([class: 'marker'] + colors.format(SKIPPED_LINES_MARKER_COLOR), SKIPPED_LINES_MARKER)
							}
						}

						if (foundLine.contextLinesAfter) {
							printContextLines builder, foundLine.contextLinesAfter,
									foundLine.contextLinesAfterOverflow, ContextPosition.AFTER, false
							prevContextLineAfterOverflow = foundLine.contextLinesAfterOverflow
						}
					}
				}
			}
		}

		htmlBodyParts << (bodyPartWriter as String)
	}

	private void printContextLines(MarkupBuilder builder, List<String> contextLines, boolean contextLinesOverflow,
			ContextPosition contextPosition, boolean prevContextLineAfterOverflow) {
		if (contextLinesOverflow) {
			if (contextPosition == ContextPosition.BEFORE) {
				if (!prevContextLineAfterOverflow) {
					contextLines.add 0, SKIPPED_LINES_MARKER
				}
			}
			else {
				contextLines.add SKIPPED_LINES_MARKER
			}
		}

		contextLines.each { contextLine ->
			builder.tr {
				td class: 'lineNr'

				if (contextLine == SKIPPED_LINES_MARKER) {
					td([class: 'marker'] + colors.format(CONTEXT_LINES_SKIPPED_LINES_MARKER_COLOR), contextLine)
				}
				else {
					td {
						pre([class: 'code'] + colors.format(CONTEXT_LINES_COLOR), contextLine)
					}
				}
			}
		}
	}

	private void colorLine(MarkupBuilder builder, String line) {
		builder.pre([class: 'code']) {
			partitioner.partition(line).each { lp ->
				span lp.colorType ? colors.format(lp.colorType) : [:], lp.text
			}
		}
	}

	private void openHtmlFile(File htmlFile) {
		try {
			Process process = ['/usr/bin/xdg-open', htmlFile.absolutePath].execute()
			def out = new StringBuffer()
			def err = new StringBuffer()
			process.consumeProcessOutput out, err
			process.waitFor()
			if (out.size() > 0) {
				println out
			}
			if (err.size() > 0) {
				println err
			}
		}
		catch (IOException e) {
			log.error "Can't open HTML file '${htmlFile}': ${e}"
		}
	}

}
