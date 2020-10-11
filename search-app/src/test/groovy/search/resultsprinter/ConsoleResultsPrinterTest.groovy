package search.resultsprinter

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import search.colors.AnsiColors
import search.conf.PatternData
import search.linefinder.FoundLine

import java.util.regex.Pattern
import java.util.stream.Stream

class ConsoleResultsPrinterTest {

	private LogMockForConsoleResultsPrinter log

	@BeforeEach
	void setup() {
		log = new LogMockForConsoleResultsPrinter()
	}

	@ParameterizedTest
	@MethodSource('canPrintSearchArgs')
	void canPrintSearch(Pattern pattern, boolean disableColors, String expectedLine) {
		// Given
		def patternData = makePatternData([pattern: pattern])
		def foundLines = makeFoundLines('This is a Test!')
		def consoleResultsPrinter = new ConsoleResultsPrinter(patternData, false, false, log, disableColors,
				new AnsiColors(disableColors))

		// When
		consoleResultsPrinter.printFoundLines('test.txt', foundLines)

		// Then
		assert log.loggedLines.any { it =~ expectedLine }
	}

	static Stream<Arguments> canPrintSearchArgs() {
		Stream.of(
				Arguments.of(~/Te?s+t/, false, /This is a .+?Test.+?!/),
				Arguments.of(~/Te?s+t/, true, /This is a Test!/),
				Arguments.of(~/(T)(e?)(s+)t/, false, /This is a .+?Test.+?!/),
				Arguments.of(~/(T)(e?)(s+)t/, true, /This is a Test!/)
		)
	}

	@ParameterizedTest
	@MethodSource('canPrintReplaceArgs')
	void canPrintReplace(Pattern pattern, boolean disableColors, String expectedLine) {
		// Given
		def patternData = makePatternData([pattern: pattern, replaceText: 'test'])
		def foundLines = makeFoundLines('This is a Test!')
		def consoleResultsPrinter = new ConsoleResultsPrinter(patternData, true, false, log, disableColors,
				new AnsiColors(disableColors))

		// When
		consoleResultsPrinter.printFoundLines('test.txt', foundLines)

		// Then
		assert log.loggedLines.any { it =~ expectedLine }
	}

	static Stream<Arguments> canPrintReplaceArgs() {
		Stream.of(
				Arguments.of(~/Te?s+t/, false, /This is a .+?test.+?!/),
				Arguments.of(~/Te?s+t/, true, /This is a test!/),
				Arguments.of(~/(T)(e?)(s+)t/, false, /This is a .+?test.+?!/),
				Arguments.of(~/(T)(e?)(s+)t/, true, /This is a test!/)
		)
	}

	private static Set<PatternData> makePatternData(Map[] patterns) {
		patterns.collect { pattern ->
			[
					searchPattern      : pattern.pattern,
					replace            : pattern.replaceText as boolean,
					replaceText        : pattern.replaceText
			] as PatternData
		}
	}

	private static List<FoundLine> makeFoundLines(String[] lines) {
		lines.collect { line ->
			[line: line] as FoundLine
		}
	}

}
