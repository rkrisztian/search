package search.resultsprinter

import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_COLORS
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_DRY_RUN
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_REPLACE
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.WITH_COLORS
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.WITH_REPLACE

import search.colors.AnsiColors
import search.conf.PatternData
import search.linefinder.FoundLine
import search.resultsprinter.linepart.LinePartitionerImpl
import search.resultsprinter.testutil.LogMockForConsoleResultsPrinter
import spock.lang.Specification

import java.util.regex.Pattern

class ConsoleResultsPrinterTest extends Specification {

	private final LogMockForConsoleResultsPrinter log = new LogMockForConsoleResultsPrinter()

	void 'does nothing if no found lines'() {
		given:
			def patternData = [new PatternData(searchPattern: ~/Te?s+t/)] as Set
			def foundLines = []
			def consoleResultsPrinter = makeConsoleResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, WITH_COLORS

		when:
			consoleResultsPrinter.printFoundLines 'test.txt', foundLines

		then:
			verifyAll(log.loggedLines) {
				it.any { it =~ /test\.txt/ }
				!it.any { it =~ /test\.txt :/ }
			}
	}

	void 'prints search results'(Pattern pattern, boolean disableColors, String expectedLine) {
		given:
			def patternData = [new PatternData(searchPattern: pattern)] as Set
			def foundLines = [new FoundLine(line: 'This is a Test!')]
			def consoleResultsPrinter = makeConsoleResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, disableColors

		when:
			consoleResultsPrinter.printFoundLines 'test.txt', foundLines

		then:
			verifyAll(log.loggedLines) {
				it.any { it =~ /test\.txt :/ }
				it.any { it =~ expectedLine }
			}

		where:
			pattern         | disableColors || expectedLine
			~/Te?s+t/       | WITH_COLORS   || /This is a .+?Test.+?!/
			~/Te?s+t/       | NO_COLORS     || /This is a Test!/
			~/(T)(e?)(s+)t/ | WITH_COLORS   || /This is a .+?Test.+?!/
			~/(T)(e?)(s+)t/ | NO_COLORS     || /This is a Test!/
	}

	void 'prints replace'(Pattern pattern, boolean disableColors, String expectedLine) {
		given:
			def patternData = [new PatternData(searchPattern: pattern, replace: true, replaceText: 'test')] as Set
			def foundLines = [new FoundLine(line: 'This is a Test!')]
			def consoleResultsPrinter = makeConsoleResultsPrinter patternData, WITH_REPLACE, NO_DRY_RUN, disableColors

		when:
			consoleResultsPrinter.printFoundLines 'test.txt', foundLines

		then:
			log.loggedLines.any { it =~ expectedLine }

		where:
			pattern         | disableColors || expectedLine
			~/Te?s+t/       | WITH_COLORS   || /This is a .+?test.+?!/
			~/Te?s+t/       | NO_COLORS     || /This is a test!/
			~/(T)(e?)(s+)t/ | WITH_COLORS   || /This is a .+?test.+?!/
			~/(T)(e?)(s+)t/ | NO_COLORS     || /This is a test!/
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
			def consoleResultsPrinter = makeConsoleResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, NO_COLORS

		when:
			consoleResultsPrinter.printFoundLines 'test.txt', foundLines

		then:
			verifyAll(log.loggedLines) {
				it.size() == 10
				it[0] =~ /test\.txt :/
				it[1] =~ /\(\.\.\.\)/
				it[2] =~ /context1/
				it[3] =~ /context2/
				it[4] =~ /5\s+:\s+.*?Test/
				it[5] =~ /context3/
				it[6] =~ /context4/
				it[7] =~ /\(\.\.\.\)/
				it[8] =~ /^$/
				it[9] =~ /^$/
			}
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
			def consoleResultsPrinter = makeConsoleResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, NO_COLORS

		when:
			consoleResultsPrinter.printFoundLines 'test.txt', foundLines

		then:
			verifyAll(log.loggedLines) {
				it.size() == 10
				it[0] =~ /test\.txt :/
				it[1] =~ /5\s+:\s+.*?Test! #1/
				it[2] =~ /context1/
				it[3] =~ /context2/
				it[4] =~ /\(\.\.\.\)/
				it[5] =~ /context3/
				it[6] =~ /context4/
				it[7] =~ /15\s+:\s+.*?Test! #2/
			}
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
			def consoleResultsPrinter = makeConsoleResultsPrinter patternData, NO_REPLACE, NO_DRY_RUN, NO_COLORS

		when:
			consoleResultsPrinter.printFoundLines 'test.txt', foundLines

		then:
			verifyAll(log.loggedLines) {
				it.size() == 9
				it[0] =~ /test\.txt :/
				it[1] =~ /5\s+:\s+.*?Test! #1/
				it[2] =~ /context1/
				it[3] =~ /context2/
				it[4] =~ /context3/
				it[5] =~ /context4/
				it[6] =~ /10\s+:\s+.*?Test! #2/
			}
	}

	private ConsoleResultsPrinter makeConsoleResultsPrinter(Set<PatternData> patternData, boolean replace, boolean dryRun,
			boolean disableColors) {
		def partitioner = new LinePartitionerImpl(patternData, replace, dryRun, disableColors)
		new ConsoleResultsPrinter(patternData, log, new AnsiColors(disableColors), partitioner, disableColors)
	}

}
