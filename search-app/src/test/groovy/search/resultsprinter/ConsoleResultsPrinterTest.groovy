package search.resultsprinter

import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_COLORS
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_DRY_RUN
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_REPLACE
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.WITH_COLORS
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.WITH_REPLACE

import org.junit.jupiter.api.BeforeEach
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

	private LogMockForConsoleResultsPrinter log

	@BeforeEach
	void setup() {
		log = new LogMockForConsoleResultsPrinter()
	}

	@ParameterizedTest(name = '[{index}] {0}; colors: {1}')
	@MethodSource('canPrintSearchArgs')
	void canPrintSearch(Pattern pattern, boolean disableColors, String expectedLine) {
		// Given
		def patternData = [new PatternData(searchPattern: pattern)] as Set
		def foundLines = [new FoundLine(line: 'This is a Test!')]
		def partitioner = new LinePartitioner(patternData, NO_REPLACE, NO_DRY_RUN, disableColors)
		def consoleResultsPrinter = new ConsoleResultsPrinter(patternData, log, disableColors, new AnsiColors(disableColors),
				partitioner)

		// When
		consoleResultsPrinter.printFoundLines('test.txt', foundLines)

		// Then
		assert log.loggedLines.any { it =~ expectedLine }
	}

	static Stream<Arguments> canPrintSearchArgs() {
		Stream.of(
				Arguments.of(~/Te?s+t/, WITH_COLORS, /This is a .+?Test.+?!/),
				Arguments.of(~/Te?s+t/, NO_COLORS, /This is a Test!/),
				Arguments.of(~/(T)(e?)(s+)t/, WITH_COLORS, /This is a .+?Test.+?!/),
				Arguments.of(~/(T)(e?)(s+)t/, NO_COLORS, /This is a Test!/)
		)
	}

	@ParameterizedTest(name = '[{index}] {0}; colors: {1}')
	@MethodSource('canPrintReplaceArgs')
	void canPrintReplace(Pattern pattern, boolean disableColors, String expectedLine) {
		// Given
		def patternData = [new PatternData(searchPattern: pattern, replace: true, replaceText: 'test')] as Set
		def foundLines = [new FoundLine(line: 'This is a Test!')]
		def partitioner = new LinePartitioner(patternData, WITH_REPLACE, NO_DRY_RUN, disableColors)
		def consoleResultsPrinter = new ConsoleResultsPrinter(patternData, log, disableColors, new AnsiColors(disableColors),
				partitioner)

		// When
		consoleResultsPrinter.printFoundLines('test.txt', foundLines)

		// Then
		assert log.loggedLines.any { it =~ expectedLine }
	}

	static Stream<Arguments> canPrintReplaceArgs() {
		Stream.of(
				Arguments.of(~/Te?s+t/, WITH_COLORS, /This is a .+?test.+?!/),
				Arguments.of(~/Te?s+t/, NO_COLORS, /This is a test!/),
				Arguments.of(~/(T)(e?)(s+)t/, WITH_COLORS, /This is a .+?test.+?!/),
				Arguments.of(~/(T)(e?)(s+)t/, NO_COLORS, /This is a test!/)
		)
	}

}
