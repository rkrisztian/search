package search.resultsprinter

import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_COLORS
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_DRY_RUN
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_REPLACE
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.WITH_COLORS
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.WITH_REPLACE

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import search.colors.HtmlColors
import search.conf.PatternData
import search.linefinder.FoundLine
import search.log.ILog
import search.log.LogMock
import search.resultsprinter.linepart.LinePartitioner
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path
import java.util.regex.Pattern

class HtmlResultsPrinterTest extends Specification {

	private final ILog log = LogMock.get()

	@TempDir
	private Path tempDir

	void 'does nothing if no found lines'() {
		given:
			def patternData = [new PatternData(searchPattern: ~/Te?s+t/)] as Set
			def foundLines = []
			def htmlResultsPrinter = makeHtmlResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, WITH_COLORS

		when:
			def html = printFoundLinesAndParse htmlResultsPrinter, foundLines

		then:
			html.depthFirst().find { it.'@data-id' == 'filePath' } =~ /test\.txt/
			!(html.depthFirst().find { it.'@data-id' == 'filePath' } =~ /test\.txt :/)
	}

	void 'prints search results'(Pattern pattern, boolean disableColors, String expectedLine) {
		given:
			def patternData = [new PatternData(searchPattern: pattern)] as Set
			def foundLines = [new FoundLine(line: 'This is a Test!')]
			def htmlResultsPrinter = makeHtmlResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, disableColors

		when:
			def html = printFoundLinesAndParse htmlResultsPrinter, foundLines

		then:
			html.depthFirst().find { it.'@data-id' == 'filePath' } =~ /test\.txt/
			html.depthFirst().find { it.'@data-id' == 'foundLine' } =~ expectedLine

		where:
			pattern         | disableColors || expectedLine
			~/Te?s+t/       | WITH_COLORS   || /This is a Test!/
			~/Te?s+t/       | NO_COLORS     || /This is a Test!/
			~/(T)(e?)(s+)t/ | WITH_COLORS   || /This is a Test!/
			~/(T)(e?)(s+)t/ | NO_COLORS     || /This is a Test!/
	}

	void 'prints replace'(Pattern pattern, boolean disableColors, String expectedLine) {
		given:
			def patternData = [new PatternData(searchPattern: pattern, replace: true, replaceText: 'test')] as Set
			def foundLines = [new FoundLine(line: 'This is a Test!')]
			def htmlResultsPrinter = makeHtmlResultsPrinter patternData, WITH_REPLACE, NO_DRY_RUN, disableColors

		when:
			def html = printFoundLinesAndParse htmlResultsPrinter, foundLines

		then:
			html.depthFirst().find { it.'@data-id' == 'foundLine' } =~ expectedLine

		where:
			pattern         | disableColors || expectedLine
			~/Te?s+t/       | WITH_COLORS    | /This is a test!/
			~/Te?s+t/       | NO_COLORS      | /This is a test!/
			~/(T)(e?)(s+)t/ | WITH_COLORS    | /This is a test!/
			~/(T)(e?)(s+)t/ | NO_COLORS      | /This is a test!/
	}

	void 'prints context lines of one matched line'() {
		given:
			def patternData = [new PatternData(searchPattern: ~/Te?s+t/)] as Set
			def foundLines = [
					new FoundLine(
							lineNr: 5,
							line: 'This is a Test!',
							contextLinesBefore: ['context1', 'context2'],
							contextLinesBeforeOverflow: true,
							contextLinesAfter: ['context3', 'context4'],
							contextLinesAfterOverflow: true
					)
			]
			def htmlResultsPrinter = makeHtmlResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, NO_COLORS

		when:
			def html = printFoundLinesAndParse htmlResultsPrinter, foundLines

		then:
			html.depthFirst().find { it.'@data-id' == 'filePath' } =~ /test\.txt/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[0] =~ /\(\.\.\.\)/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[1] =~ /context1/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[2] =~ /context2/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[3] =~ /5.*?Test/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[4] =~ /context3/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[5] =~ /context4/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[6] =~ /\(\.\.\.\)/
	}

	void 'prints context lines between two matched lines, with overflow'() {
		given:
			def patternData = [new PatternData(searchPattern: ~/Te?s+t/)] as Set
			def foundLines = [
					new FoundLine(
							lineNr: 5,
							line: 'This is a Test! #1',
							contextLinesAfter: ['context1', 'context2'],
							contextLinesAfterOverflow: true
					),
					new FoundLine(
							lineNr: 15,
							line: 'This is a Test! #2',
							contextLinesBefore: ['context3', 'context4']
					)
			]
			def htmlResultsPrinter = makeHtmlResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, NO_COLORS

		when:
			def html = printFoundLinesAndParse htmlResultsPrinter, foundLines

		then:
			html.depthFirst().find { it.'@data-id' == 'filePath' } =~ /test\.txt/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[0] =~ /5.*?Test! #1/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[1] =~ /context1/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[2] =~ /context2/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[3] =~ /\(\.\.\.\)/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[4] =~ /context3/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[5] =~ /context4/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[6] =~ /15.*?Test! #2/
	}

	void 'prints context lines between two matched lines, without overflow'() {
		given:
			def patternData = [new PatternData(searchPattern: ~/Te?s+t/)] as Set
			def foundLines = [
					new FoundLine(
							lineNr: 5,
							line: 'This is a Test! #1',
							contextLinesAfter: ['context1', 'context2']
					),
					new FoundLine(
							lineNr: 10,
							line: 'This is a Test! #2',
							contextLinesBefore: ['context3', 'context4']
					)
			]
			def htmlResultsPrinter = makeHtmlResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, NO_COLORS

		when:
			def html = printFoundLinesAndParse htmlResultsPrinter, foundLines

		then:
			html.depthFirst().find { it.'@data-id' == 'filePath' } =~ /test\.txt/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[0] =~ /5.*?Test! #1/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[1] =~ /context1/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[2] =~ /context2/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[3] =~ /context3/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[4] =~ /context4/
			html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[5] =~ /10.*?Test! #2/
	}

	void 'prints empty lines'() {
		given:
			def patternData = [new PatternData(searchPattern: ~/^$/)] as Set
			def foundLines = [new FoundLine(line: '', lineNr: 1, contextLinesAfter: [''])]
			def htmlResultsPrinter = makeHtmlResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, NO_COLORS

		when:
			def html = printFoundLinesAndParse htmlResultsPrinter, foundLines

		then:
			html.depthFirst().find { it.'@data-id' == 'foundLine' } =~ /(?m)1\s*$/
			html.depthFirst().find { it.'@data-id' == 'contextLine' } =~ /(?m)\s*${160 as char}\s*$/
	}

	private HtmlResultsPrinter makeHtmlResultsPrinter(Set<PatternData> patternData, boolean replace, boolean dryRun,
			boolean disableColors) {
		def partitioner = new LinePartitioner(patternData, replace, dryRun, disableColors)
		new HtmlResultsPrinter(patternData, log, new HtmlColors(disableColors), partitioner, tempDir)
	}

	private static GPathResult printFoundLinesAndParse(HtmlResultsPrinter htmlResultsPrinter, List<FoundLine> foundLines) {
		htmlResultsPrinter.printFoundLines 'test.txt', foundLines
		def htmlFile = htmlResultsPrinter.writeToHtmlFile()
		new XmlSlurper().parse htmlFile
	}

}
