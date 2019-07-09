package search.resultsprinter

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import search.colors.AnsiColors
import search.conf.PatternData
import search.linefinder.FoundLine

class ConsoleResultsPrinterTest {

	private LogMockForConsoleResultsPrinter log

	@BeforeEach
	void setup() {
		log = new LogMockForConsoleResultsPrinter()
	}

	@Test
	void canPrintSearchWithoutSubgroups_withColors() {
		// Given
		def patternData = createPatternData([pattern: ~/Te?s+t/])
		def foundLines = createFoundLines('This is a Test!')
		def consoleResultsPrinter = new ConsoleResultsPrinter(patternData, false, false, log, false,
				new AnsiColors(false))

		// When
		consoleResultsPrinter.printFoundLines('test.txt', foundLines)

		// Then
		assert log.loggedLines.any { it =~ /This is a .+?Test.+?!/ }
	}

	@Test
	void canPrintSearchWithoutSubgroups_withoutColors() {
		// Given
		def patternData = createPatternData([pattern: ~/Te?s+t/])
		def foundLines = createFoundLines('This is a Test!')
		def consoleResultsPrinter = new ConsoleResultsPrinter(patternData, false, false, log, true,
				new AnsiColors(true))

		// When
		consoleResultsPrinter.printFoundLines('test.txt', foundLines)

		// Then
		assert log.loggedLines.any { it =~ /This is a Test!/ }
	}

	@Test
	void canPrintSearchWithSubgroups_withColors() {
		// Given
		def patternData = createPatternData([pattern: ~/(T)(e?)(s+)t/])
		def foundLines = createFoundLines('This is a Test!')
		def consoleResultsPrinter = new ConsoleResultsPrinter(patternData, false, false, log, false,
				new AnsiColors(false))

		// When
		consoleResultsPrinter.printFoundLines('test.txt', foundLines)

		// Then
		assert log.loggedLines.any { it =~ /This is a .+?Test.+?!/ }
	}

	@Test
	void canPrintSearchWithSubgroups_withoutColors() {
		// Given
		def patternData = createPatternData([pattern: ~/(T)(e?)(s+)t/])
		def foundLines = createFoundLines('This is a Test!')
		def consoleResultsPrinter = new ConsoleResultsPrinter(patternData, false, false, log, true,
				new AnsiColors(true))

		// When
		consoleResultsPrinter.printFoundLines('test.txt', foundLines)

		// Then
		assert log.loggedLines.any { it =~ /This is a Test!/ }
	}

	@Test
	void canPrintReplaceWithoutSubgroups_withColors() {
		// Given
		def patternData = createPatternData([pattern: ~/Te?s+t/, replaceText: 'test'])
		def foundLines = createFoundLines('This is a Test!')
		def consoleResultsPrinter = new ConsoleResultsPrinter(patternData, true, false, log, false,
				new AnsiColors(false))

		// When
		consoleResultsPrinter.printFoundLines('test.txt', foundLines)

		// Then
		assert log.loggedLines.any { it =~ /This is a .+?test.+?!/ }
	}

	@Test
	void canPrintReplaceWithoutSubgroups_withoutColors() {
		// Given
		def patternData = createPatternData([pattern: ~/Te?s+t/, replaceText: 'test'])
		def foundLines = createFoundLines('This is a Test!')
		def consoleResultsPrinter = new ConsoleResultsPrinter(patternData, true, false, log, true,
				new AnsiColors(true))

		// When
		consoleResultsPrinter.printFoundLines('test.txt', foundLines)

		// Then
		assert log.loggedLines.any { it =~ /This is a test!/ }
	}

	@Test
	void canPrintReplaceWithSubgroups_withColors() {
		// Given
		def patternData = createPatternData([pattern: ~/(T)(e?)(s+)t/, replaceText: 'test'])
		def foundLines = createFoundLines('This is a Test!')
		def consoleResultsPrinter = new ConsoleResultsPrinter(patternData, true, false, log, false,
				new AnsiColors(false))

		// When
		consoleResultsPrinter.printFoundLines('test.txt', foundLines)

		// Then
		assert log.loggedLines.any { it =~ /This is a .+?test.+?!/ }
	}

	@Test
	void canPrintReplaceWithSubgroups_withoutColors() {
		// Given
		def patternData = createPatternData([pattern: ~/(T)(e?)(s+)t/, replaceText: 'test'])
		def foundLines = createFoundLines('This is a Test!')
		def consoleResultsPrinter = new ConsoleResultsPrinter(patternData, true, false, log, true,
				new AnsiColors(true))

		// When
		consoleResultsPrinter.printFoundLines('test.txt', foundLines)

		// Then
		assert log.loggedLines.any { it =~ /This is a test!/ }
	}

	private Set<PatternData> createPatternData(Map[] patterns) {
		patterns.collect { pattern ->
			[
					searchPattern      : pattern.pattern,
					colorReplacePattern: pattern.pattern,
					replace            : pattern.replaceText as boolean,
					replaceText        : pattern.replaceText
			] as PatternData
		}
	}

	private List<FoundLine> createFoundLines(String[] lines) {
		lines.collect { line ->
			[line: line] as FoundLine
		}
	}

}
