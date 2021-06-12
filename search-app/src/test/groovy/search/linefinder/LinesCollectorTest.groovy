package search.linefinder

import static org.junit.jupiter.api.Assertions.assertThrows
import static search.linefinder.LineVisibility.HIDE
import static search.linefinder.LineVisibility.SHOW
import static search.testutil.GroovyAssertions.assertAll

import org.junit.jupiter.api.Test

class LinesCollectorTest {

	private static final String TEST_LINE_CONTEXT_BEFORE_BEFORE = 'context before before'
	private static final String TEST_LINE_CONTEXT_BEFORE = 'context before'
	private static final String TEST_LINE = 'test'
	private static final int TEST_LINENR = 3
	private static final String TEST_LINE_CONTEXT_AFTER = 'context after'
	private static final String TEST_LINE_CONTEXT_AFTER_AFTER = 'context after after'

	private LinesCollector linesCollector

	@Test
	void matchedLinesAndContextDisabled_foundOneMatchedLine_shouldYieldEmptyList() {
		// Given
		linesCollector = new LinesCollector(0, 0, 100)

		// When
		linesCollector.storeFoundLine TEST_LINENR, TEST_LINE, SHOW

		// Then
		assertAll(
				{ assert !linesCollector.foundLines },
				{ assert !linesCollector.currentContextLinesBefore }
		)
	}

	@Test
	void matchedLinesAndContextDisabled_foundOneContextLine_shouldYieldEmptyList() {
		// Given
		linesCollector = new LinesCollector(0, 0, 100)

		// When
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE

		// Then
		assertAll(
				{ assert !linesCollector.foundLines },
				{ assert !linesCollector.currentContextLinesBefore }
		)
	}

	@Test
	void noMatchedLinesAndOneContextLineEnabled_shouldYieldEmptyList() {
		// Given
		linesCollector = new LinesCollector(0, 1, 100)

		// When
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE

		// Then
		assertAll(
				{ assert !linesCollector.foundLines },
				{ assert !linesCollector.currentContextLinesBefore }
		)
	}

	@Test
	void oneMatchedLineAndNoContextEnabled_shouldYieldOneLine() {
		// Given
		linesCollector = new LinesCollector(1, 0, 100)

		// When
		linesCollector.with {
			storeContextLine TEST_LINE_CONTEXT_BEFORE
			storeFoundLine TEST_LINENR, TEST_LINE, SHOW
			storeContextLine TEST_LINE_CONTEXT_AFTER
		}

		// Then
		assertAll(
				{ assert linesCollector.foundLines == [new FoundLine(line: TEST_LINE, lineNr: TEST_LINENR)] },
				{ assert !linesCollector.currentContextLinesBefore }
		)
	}

	@Test
	void oneMatchedLongLine_shouldYieldOneTruncatedLine() {
		// Given
		linesCollector = new LinesCollector(1, 0, 20)

		// When
		linesCollector.storeFoundLine TEST_LINENR, 'this is a very long line that should be truncated', SHOW

		// Then
		assert linesCollector.foundLines == [new FoundLine(line: 'this is a very long ...', lineNr: TEST_LINENR)]
	}

	@Test
	void oneMatchedLineAndOneContextLineEnabled_singleMatch_withoutOverflow() {
		// Given
		linesCollector = new LinesCollector(1, 1, 100)

		// When
		linesCollector.with {
			storeContextLine TEST_LINE_CONTEXT_BEFORE
			storeFoundLine TEST_LINENR, TEST_LINE, SHOW
			storeContextLine TEST_LINE_CONTEXT_AFTER
		}

		// Then
		assertAll(
				{
					assert linesCollector.foundLines == [new FoundLine(
							line: TEST_LINE,
							lineNr: TEST_LINENR,
							contextLinesBefore: [TEST_LINE_CONTEXT_BEFORE],
							contextLinesAfter: [TEST_LINE_CONTEXT_AFTER]
					)]
				},
				{ assert !linesCollector.currentContextLinesBefore }
		)
	}

	@Test
	void oneMatchedLineAndOneContextLineEnabled_singleMatch_withOverflow() {
		// Given
		linesCollector = new LinesCollector(1, 1, 100)

		// When
		linesCollector.with {
			storeContextLine TEST_LINE_CONTEXT_BEFORE_BEFORE
			storeContextLine TEST_LINE_CONTEXT_BEFORE
			storeFoundLine TEST_LINENR, TEST_LINE, SHOW
			storeContextLine TEST_LINE_CONTEXT_AFTER
			storeContextLine TEST_LINE_CONTEXT_AFTER_AFTER
		}

		// Then
		assertAll(
				{
					assert linesCollector.foundLines == [new FoundLine(
							line: TEST_LINE,
							lineNr: TEST_LINENR,
							contextLinesBefore: [TEST_LINE_CONTEXT_BEFORE],
							contextLinesAfter: [TEST_LINE_CONTEXT_AFTER],
							contextLinesBeforeOverflow: true,
							contextLinesAfterOverflow: true
					)]
				},
				{ assert !linesCollector.currentContextLinesBefore }
		)
	}

	@Test
	void oneMatchedLineAndOneContextLineEnabled_reset() {
		// Given
		linesCollector = new LinesCollector(1, 1, 100)

		// When
		linesCollector.with {
			storeContextLine TEST_LINE_CONTEXT_BEFORE
			storeFoundLine TEST_LINENR, TEST_LINE, SHOW
			storeContextLine TEST_LINE_CONTEXT_AFTER
			reset()
		}

		// Then
		assertAll(
				{ assert !linesCollector.foundLines },
				{ assert !linesCollector.currentContextLinesBefore }
		)
	}

	@Test
	void oneMatchedLineAndOneContextLineEnabled_oneMatchDisplayed() {
		// Given
		linesCollector = new LinesCollector(1, 1, 100)

		// When
		linesCollector.with {
			storeContextLine 'c1'
			storeFoundLine 2, 'f1', SHOW
			storeContextLine 'c2'
			storeFoundLine 4, 'f2', SHOW
		}

		// Then
		assertAll(
				{ assert linesCollector.hasFinished() },
				{
					assert linesCollector.foundLines == [
							new FoundLine(
									line: 'f1',
									lineNr: 2,
									contextLinesBefore: ['c1'],
									contextLinesAfter: ['c2'],
									contextLinesAfterOverflow: true
							)
					]
				},
				{ assert !linesCollector.currentContextLinesBefore }
		)
	}

	@Test
	void oneMatchedLineAndOneContextLineEnabled_oneMatch_hidden() {
		// Given
		linesCollector = new LinesCollector(1, 1, 100)

		// When
		linesCollector.with {
			storeContextLine 'c1'
			storeFoundLine 2, 'f1', HIDE
			storeContextLine 'c2'
		}

		// Then
		assert !linesCollector.foundLines
	}

	@Test
	void oneMatchedLineAndOneContextLineEnabled_twoMatches_oneHidden() {
		// Given
		linesCollector = new LinesCollector(1, 1, 100)

		// When
		linesCollector.with {
			storeContextLine 'c1'
			storeFoundLine 2, 'f1', SHOW
			storeContextLine 'c2'
			storeFoundLine 4, 'f2', HIDE
			storeContextLine 'c3'
		}

		// Then
		assertAll(
				{
					assert linesCollector.foundLines == [new FoundLine(
							line: 'f1',
							lineNr: 2,
							contextLinesBefore: ['c1'],
							contextLinesAfter: ['c2'],
							contextLinesAfterOverflow: true
					)]
				},
				{ assert !linesCollector.currentContextLinesBefore }
		)
	}

	@Test
	void twoMatchedLinesAndOneContextLineEnabled() {
		// Given
		linesCollector = new LinesCollector(2, 1, 100)

		// When
		linesCollector.with {
			storeContextLine 'c1'
			storeFoundLine 2, 'f1', SHOW
			storeContextLine 'c2'
			storeFoundLine 4, 'f2', SHOW
			storeContextLine 'c3'
		}

		// Then
		assertAll(
				{
					assert linesCollector.foundLines == [
							new FoundLine(
									line: 'f1',
									lineNr: 2,
									contextLinesBefore: ['c1'],
									contextLinesAfter: ['c2']
							),
							new FoundLine(
									line: 'f2',
									lineNr: 4,
									contextLinesBefore: [],
									contextLinesAfter: ['c3']
							)
					]
				},
				{ assert !linesCollector.currentContextLinesBefore }
		)
	}

	@Test
	void matchedLineShouldBeDisplayedAsContextLineAfterTheLimit_mixed() {
		// Given
		linesCollector = new LinesCollector(1, 2, 100)

		// When
		linesCollector.with {
			storeContextLine 'c1'
			storeFoundLine 2, 'f1', SHOW
			storeContextLine 'c2'
			storeFoundLine 4, 'f2', SHOW
			storeContextLine 'c3'
		}

		// Then
		assert linesCollector.foundLines == [
				new FoundLine(
						line: 'f1',
						lineNr: 2,
						contextLinesBefore: ['c1'],
						contextLinesAfter: ['c2', 'f2'],
						contextLinesAfterOverflow: true
				)
		]
	}

	@Test
	void matchedLineShouldBeDisplayedAsContextLineAfterTheLimit_onlyFoundLines() {
		// Given
		linesCollector = new LinesCollector(1, 2, 100)

		// When
		linesCollector.with {
			storeFoundLine 2, 'f1', SHOW
			storeFoundLine 3, 'f2', SHOW
			storeFoundLine 4, 'f3', SHOW
		}

		// Then
		assert linesCollector.foundLines == [
				new FoundLine(
						line: 'f1',
						lineNr: 2,
						contextLinesAfter: ['f2', 'f3'],
						contextLinesAfterOverflow: false
				)
		]
	}

	@Test
	void unlimitedMatchesAndOneContextLineEnabled() {
		// Given
		linesCollector = new LinesCollector(-1, 1, 100)

		// When
		linesCollector.with {
			storeContextLine 'c1'
			storeFoundLine 2, 'f1', SHOW
			storeContextLine 'c2'
			storeFoundLine 4, 'f2', SHOW
			storeContextLine 'c3'
			storeContextLine 'c4'
		}

		// Then
		assertAll(
				{
					assert linesCollector.foundLines == [
							new FoundLine(
									line: 'f1',
									lineNr: 2,
									contextLinesBefore: ['c1'],
									contextLinesAfter: ['c2']
							),
							new FoundLine(
									line: 'f2',
									lineNr: 4,
									contextLinesBefore: [],
									contextLinesAfter: ['c3'],
									contextLinesAfterOverflow: true
							)
					]
				},
				{ assert linesCollector.currentContextLinesBefore }
		)
	}

	@Test
	void shouldNotBeOverflowBetweenJustEnoughContextLines() {
		// Given
		linesCollector = new LinesCollector(-1, 2, 100)

		// When
		linesCollector.with {
			storeFoundLine 1, 'f1', SHOW
			storeContextLine 'c1'
			storeContextLine 'c2'
			storeContextLine 'c3'
			storeContextLine 'c4'
			storeFoundLine 6, 'f2', SHOW
		}

		// Then
		assert linesCollector.foundLines == [
				new FoundLine(
						line: 'f1',
						lineNr: 1,
						contextLinesBefore: [],
						contextLinesAfter: ['c1', 'c2']
				),
				new FoundLine(
						line: 'f2',
						lineNr: 6,
						contextLinesBefore: ['c3', 'c4'],
						contextLinesAfter: []
				)
		]
	}

	@Test
	void shouldOverflowBeforeAndAfterWithTooManyContextLines() {
		// Given
		linesCollector = new LinesCollector(-1, 2, 100)

		// When
		linesCollector.with {
			storeFoundLine 1, 'f1', SHOW
			storeContextLine 'c1'
			storeContextLine 'c2'
			storeContextLine 'c3'
			storeContextLine 'c4'
			storeContextLine 'c5'
			storeContextLine 'c6'
			storeFoundLine 8, 'f2', SHOW
		}

		// Then
		assert linesCollector.foundLines == [
				new FoundLine(
						line: 'f1',
						lineNr: 1,
						contextLinesBefore: [],
						contextLinesAfter: ['c1', 'c2'],
						contextLinesAfterOverflow: true
				),
				new FoundLine(
						line: 'f2',
						lineNr: 8,
						contextLinesBefore: ['c5', 'c6'],
						contextLinesAfter: [],
						contextLinesBeforeOverflow: true
				)
		]
	}

	@Test
	void doNotAcceptFoundLinesWhenSearchFinished() {
		// Given
		linesCollector = new LinesCollector(1, 1, 100)

		// When
		linesCollector.with {
			storeContextLine 'c1'
			storeFoundLine 2, 'f1', SHOW
			storeContextLine 'c2'
			storeFoundLine 4, 'f2', SHOW
		}

		// Then
		assertThrows(LinesCollector.LinesCollectorException) {
			linesCollector.storeFoundLine 5, 'f3', SHOW
		}
	}

	@Test
	void doNotAcceptContextLinesWhenSearchFinished() {
		// Given
		linesCollector = new LinesCollector(1, 1, 100)

		// When
		linesCollector.with {
			storeContextLine 'c1'
			storeFoundLine 2, 'f1', SHOW
			storeContextLine 'c2'
			storeFoundLine 4, 'f2', SHOW
		}

		// Then
		assertThrows(LinesCollector.LinesCollectorException) {
			linesCollector.storeContextLine 'c3'
		}
	}

}
