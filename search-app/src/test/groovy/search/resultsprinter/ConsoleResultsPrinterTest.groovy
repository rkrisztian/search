package search.resultsprinter

import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_COLORS
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_DRY_RUN
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_REPLACE
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.WITH_COLORS
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.WITH_REPLACE
import static search.testutil.GroovyAssertions.assertAll

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import search.colors.AnsiColors
import search.conf.PatternData
import search.linefinder.FoundLine
import search.resultsprinter.linepart.LinePartitioner
import search.resultsprinter.testutil.LogMockForConsoleResultsPrinter

import java.util.regex.Pattern
import java.util.stream.Stream

class ConsoleResultsPrinterTest {

	private final LogMockForConsoleResultsPrinter log = new LogMockForConsoleResultsPrinter()

	@Test
	void doesNothingIfNoFoundLines() {
		// Given
		def patternData = [new PatternData(searchPattern: ~/Te?s+t/)] as Set
		def foundLines = []
		def consoleResultsPrinter = makeConsoleResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, WITH_COLORS

		// When
		consoleResultsPrinter.printFoundLines 'test.txt', foundLines

		// Then
		assertAll(
				{ assert log.loggedLines.any { it =~ /test\.txt/ } },
				{ assert !log.loggedLines.any { it =~ /test\.txt :/ } }
		)
	}

	@ParameterizedTest(name = '[{index}] {0}; colors: {1}')
	@MethodSource('printsSearchResultsArgs')
	void printsSearchResults(Pattern pattern, boolean disableColors, String expectedLine) {
		// Given
		def patternData = [new PatternData(searchPattern: pattern)] as Set
		def foundLines = [new FoundLine(line: 'This is a Test!')]
		def consoleResultsPrinter = makeConsoleResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, disableColors

		// When
		consoleResultsPrinter.printFoundLines 'test.txt', foundLines

		// Then
		assertAll(
				{ assert log.loggedLines.any { it =~ /test\.txt :/ } },
				{ assert log.loggedLines.any { it =~ expectedLine } }
		)
	}

	@SuppressWarnings('unused')
	static Stream<Arguments> printsSearchResultsArgs() {
		Stream.of(
				Arguments.of(~/Te?s+t/, WITH_COLORS, /This is a .+?Test.+?!/),
				Arguments.of(~/Te?s+t/, NO_COLORS, /This is a Test!/),
				Arguments.of(~/(T)(e?)(s+)t/, WITH_COLORS, /This is a .+?Test.+?!/),
				Arguments.of(~/(T)(e?)(s+)t/, NO_COLORS, /This is a Test!/)
		)
	}

	@ParameterizedTest(name = '[{index}] {0}; colors: {1}')
	@MethodSource('printsReplaceArgs')
	void printsReplace(Pattern pattern, boolean disableColors, String expectedLine) {
		// Given
		def patternData = [new PatternData(searchPattern: pattern, replace: true, replaceText: 'test')] as Set
		def foundLines = [new FoundLine(line: 'This is a Test!')]
		def consoleResultsPrinter = makeConsoleResultsPrinter patternData, WITH_REPLACE, NO_DRY_RUN, disableColors

		// When
		consoleResultsPrinter.printFoundLines 'test.txt', foundLines

		// Then
		assert log.loggedLines.any { it =~ expectedLine }
	}

	@SuppressWarnings('unused')
	static Stream<Arguments> printsReplaceArgs() {
		Stream.of(
				Arguments.of(~/Te?s+t/, WITH_COLORS, /This is a .+?test.+?!/),
				Arguments.of(~/Te?s+t/, NO_COLORS, /This is a test!/),
				Arguments.of(~/(T)(e?)(s+)t/, WITH_COLORS, /This is a .+?test.+?!/),
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
		def consoleResultsPrinter = makeConsoleResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, NO_COLORS

		// When
		consoleResultsPrinter.printFoundLines 'test.txt', foundLines

		// Then
		assertAll(
				{ assert log.loggedLines.size() == 9 },
				{ assert log.loggedLines[0] =~ /test\.txt :/ },
				{ assert log.loggedLines[1] =~ /\(\.\.\.\)/ },
				{ assert log.loggedLines[2] =~ /context1/ },
				{ assert log.loggedLines[3] =~ /context2/ },
				{ assert log.loggedLines[4] =~ /5\s+:\s+.*?Test/ },
				{ assert log.loggedLines[5] =~ /context3/ },
				{ assert log.loggedLines[6] =~ /context4/ },
				{ assert log.loggedLines[7] =~ /\(\.\.\.\)/ },
				{ assert log.loggedLines[8] =~ /^$/ }
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
		def consoleResultsPrinter = makeConsoleResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, NO_COLORS

		// When
		consoleResultsPrinter.printFoundLines 'test.txt', foundLines

		// Then
		assertAll(
				{ assert log.loggedLines.size() == 9 },
				{ assert log.loggedLines[0] =~ /test\.txt :/ },
				{ assert log.loggedLines[1] =~ /5\s+:\s+.*?Test! #1/ },
				{ assert log.loggedLines[2] =~ /context1/ },
				{ assert log.loggedLines[3] =~ /context2/ },
				{ assert log.loggedLines[4] =~ /\(\.\.\.\)/ },
				{ assert log.loggedLines[5] =~ /context3/ },
				{ assert log.loggedLines[6] =~ /context4/ },
				{ assert log.loggedLines[7] =~ /15\s+:\s+.*?Test! #2/ }
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
		def consoleResultsPrinter = makeConsoleResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, NO_COLORS

		// When
		consoleResultsPrinter.printFoundLines 'test.txt', foundLines

		// Then
		assertAll(
				{ assert log.loggedLines.size() == 8 },
				{ assert log.loggedLines[0] =~ /test\.txt :/ },
				{ assert log.loggedLines[1] =~ /5\s+:\s+.*?Test! #1/ },
				{ assert log.loggedLines[2] =~ /context1/ },
				{ assert log.loggedLines[3] =~ /context2/ },
				{ assert log.loggedLines[4] =~ /context3/ },
				{ assert log.loggedLines[5] =~ /context4/ },
				{ assert log.loggedLines[6] =~ /10\s+:\s+.*?Test! #2/ }
		)
	}

	private ConsoleResultsPrinter makeConsoleResultsPrinter(Set<PatternData> patternData, boolean replace, boolean dryRun,
			boolean disableColors) {
		def partitioner = new LinePartitioner(patternData, replace, dryRun, disableColors)
		new ConsoleResultsPrinter(patternData, log, new AnsiColors(disableColors), partitioner, disableColors)
	}

}
