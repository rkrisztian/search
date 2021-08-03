package search.resultsprinter

import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_COLORS
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_DRY_RUN
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_REPLACE
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.WITH_COLORS
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.WITH_REPLACE
import static search.testutil.GroovyAssertions.assertAll

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import search.colors.HtmlColors
import search.conf.PatternData
import search.linefinder.FoundLine
import search.log.ILog
import search.log.LogMock
import search.resultsprinter.linepart.LinePartitioner

import java.nio.file.Path
import java.util.regex.Pattern
import java.util.stream.Stream

class HtmlResultsPrinterTest {

	private final ILog log = LogMock.get()

	@TempDir
	protected Path tempDir

	@Test
	void doesNothingIfNoFoundLines() {
		// Given
		def patternData = [new PatternData(searchPattern: ~/Te?s+t/)] as Set
		def foundLines = []
		def htmlResultsPrinter = makeHtmlResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, WITH_COLORS

		// When
		def html = printFoundLinesAndParse htmlResultsPrinter, foundLines

		// Then
		assertAll(
				{ assert html.depthFirst().find { it.'@data-id' == 'filePath' } =~ /test\.txt/ },
				{ assert !(html.depthFirst().find { it.'@data-id' == 'filePath' } =~ /test\.txt :/) }
		)
	}

	@ParameterizedTest(name = '[{index}] {0}; colors: {1}')
	@MethodSource('printsSearchResultsArgs')
	void printsSearchResults(Pattern pattern, boolean disableColors, String expectedLine) {
		// Given
		def patternData = [new PatternData(searchPattern: pattern)] as Set
		def foundLines = [new FoundLine(line: 'This is a Test!')]
		def htmlResultsPrinter = makeHtmlResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, disableColors

		// When
		def html = printFoundLinesAndParse htmlResultsPrinter, foundLines

		// Then
		assertAll(
				{ assert html.depthFirst().find { it.'@data-id' == 'filePath' } =~ /test\.txt/ },
				{ assert html.depthFirst().find { it.'@data-id' == 'foundLine' } =~ expectedLine }
		)
	}

	@SuppressWarnings('unused')
	static Stream<Arguments> printsSearchResultsArgs() {
		Stream.of(
				Arguments.of(~/Te?s+t/, WITH_COLORS, /This is a Test!/),
				Arguments.of(~/Te?s+t/, NO_COLORS, /This is a Test!/),
				Arguments.of(~/(T)(e?)(s+)t/, WITH_COLORS, /This is a Test!/),
				Arguments.of(~/(T)(e?)(s+)t/, NO_COLORS, /This is a Test!/)
		)
	}

	@ParameterizedTest(name = '[{index}] {0}; colors: {1}')
	@MethodSource('printsReplaceArgs')
	void printsReplace(Pattern pattern, boolean disableColors, String expectedLine) {
		// Given
		def patternData = [new PatternData(searchPattern: pattern, replace: true, replaceText: 'test')] as Set
		def foundLines = [new FoundLine(line: 'This is a Test!')]
		def htmlResultsPrinter = makeHtmlResultsPrinter patternData, WITH_REPLACE, NO_DRY_RUN, disableColors

		// When
		def html = printFoundLinesAndParse htmlResultsPrinter, foundLines

		// Then
		assert html.depthFirst().find { it.'@data-id' == 'foundLine' } =~ expectedLine
	}

	@SuppressWarnings('unused')
	static Stream<Arguments> printsReplaceArgs() {
		Stream.of(
				Arguments.of(~/Te?s+t/, WITH_COLORS, /This is a test!/),
				Arguments.of(~/Te?s+t/, NO_COLORS, /This is a test!/),
				Arguments.of(~/(T)(e?)(s+)t/, WITH_COLORS, /This is a test!/),
				Arguments.of(~/(T)(e?)(s+)t/, NO_COLORS, /This is a test!/)
		)
	}

	@Test
	void printsContextLinesOfOneMatchedLine() {
		// Given
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

		// When
		def html = printFoundLinesAndParse htmlResultsPrinter, foundLines

		// Then
		assertAll(
				{ assert html.depthFirst().find { it.'@data-id' == 'filePath' } =~ /test\.txt/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[0] =~ /\(\.\.\.\)/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[1] =~ /context1/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[2] =~ /context2/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[3] =~ /5.*?Test/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[4] =~ /context3/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[5] =~ /context4/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[6] =~ /\(\.\.\.\)/ }
		)
	}

	@Test
	void printsContextLinesBetweenTwoMatchedLines_withOverflow() {
		// Given
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

		// When
		def html = printFoundLinesAndParse htmlResultsPrinter, foundLines

		// Then
		assertAll(
				{ assert html.depthFirst().find { it.'@data-id' == 'filePath' } =~ /test\.txt/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[0] =~ /5.*?Test! #1/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[1] =~ /context1/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[2] =~ /context2/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[3] =~ /\(\.\.\.\)/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[4] =~ /context3/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[5] =~ /context4/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[6] =~ /15.*?Test! #2/ }
		)
	}

	@Test
	void printsContextLinesBetweenTwoMatchedLines_withoutOverflow() {
		// Given
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

		// When
		def html = printFoundLinesAndParse htmlResultsPrinter, foundLines

		// Then
		assertAll(
				{ assert html.depthFirst().find { it.'@data-id' == 'filePath' } =~ /test\.txt/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[0] =~ /5.*?Test! #1/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[1] =~ /context1/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[2] =~ /context2/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[3] =~ /context3/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[4] =~ /context4/ },
				{ assert html.depthFirst().findAll { it.'@data-id' =~ /(found|context)Line/ }[5] =~ /10.*?Test! #2/ }
		)
	}

	@Test
	void printsEmptyLines() {
		// Given
		def patternData = [new PatternData(searchPattern: ~/^$/)] as Set
		def foundLines = [new FoundLine(line: '', lineNr: 1, contextLinesAfter: [''])]
		def htmlResultsPrinter = makeHtmlResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, NO_COLORS

		// When
		def html = printFoundLinesAndParse htmlResultsPrinter, foundLines

		// Then
		assertAll(
				{ assert html.depthFirst().find { it.'@data-id' == 'foundLine' } =~ /(?m)1\s*$/ },
				{ assert html.depthFirst().find { it.'@data-id' == 'contextLine' } =~ /(?m)\s*${160 as char}\s*$/ }
		)
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
