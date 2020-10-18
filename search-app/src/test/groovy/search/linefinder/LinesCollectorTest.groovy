package search.linefinder

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
	void matchedLinesAndContextDisabled_shouldYieldEmptyList() {
		// Given
		linesCollector = new LinesCollector(0, 0, 100)

		// When
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE
		linesCollector.storeFoundLine TEST_LINENR, TEST_LINE, SHOW
		linesCollector.storeContextLine TEST_LINE_CONTEXT_AFTER

		// Then
		assertAll(
				{ assert !linesCollector.foundLines },
				{ assert !linesCollector.currentContextLinesBefore }
		)
	}

	@Test
	void noMatchedLinesAndOneContextLineEnabled_singleMatch() {
		// Given
		linesCollector = new LinesCollector(0, 1, 100)

		// When
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE
		linesCollector.storeFoundLine TEST_LINENR, TEST_LINE, SHOW
		linesCollector.storeContextLine TEST_LINE_CONTEXT_AFTER

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
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE
		linesCollector.storeFoundLine TEST_LINENR, TEST_LINE, SHOW
		linesCollector.storeContextLine TEST_LINE_CONTEXT_AFTER

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
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE
		linesCollector.storeFoundLine TEST_LINENR, TEST_LINE, SHOW
		linesCollector.storeContextLine TEST_LINE_CONTEXT_AFTER

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
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE_BEFORE
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE
		linesCollector.storeFoundLine TEST_LINENR, TEST_LINE, SHOW
		linesCollector.storeContextLine TEST_LINE_CONTEXT_AFTER
		linesCollector.storeContextLine TEST_LINE_CONTEXT_AFTER_AFTER

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
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE
		linesCollector.storeFoundLine TEST_LINENR, TEST_LINE, SHOW
		linesCollector.storeContextLine TEST_LINE_CONTEXT_AFTER
		linesCollector.reset()

		// Then
		assertAll(
				{ assert !linesCollector.foundLines },
				{ assert !linesCollector.currentContextLinesBefore }
		)
	}

	@Test
	void oneMatchedLineAndOneContextLineEnabled_twoMatches() {
		// Given
		linesCollector = new LinesCollector(1, 1, 100)

		// When
		linesCollector.storeContextLine 'c1'
		linesCollector.storeFoundLine 2, 'f1', SHOW
		linesCollector.storeContextLine 'c2'
		linesCollector.storeFoundLine 4, 'f2', SHOW
		linesCollector.storeContextLine 'c3'

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
									line: '',
									lineNr: -1
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
		linesCollector.storeContextLine 'c1'
		linesCollector.storeFoundLine 2, 'f1', HIDE
		linesCollector.storeContextLine 'c2'

		// Then
		assert !linesCollector.foundLines
	}

	@Test
	void oneMatchedLineAndOneContextLineEnabled_twoMatches_oneHidden() {
		// Given
		linesCollector = new LinesCollector(1, 1, 100)

		// When
		linesCollector.storeContextLine 'c1'
		linesCollector.storeFoundLine 2, 'f1', SHOW
		linesCollector.storeContextLine 'c2'
		linesCollector.storeFoundLine 4, 'f2', HIDE
		linesCollector.storeContextLine 'c3'

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
		linesCollector.storeContextLine 'c1'
		linesCollector.storeFoundLine 2, 'f1', SHOW
		linesCollector.storeContextLine 'c2'
		linesCollector.storeFoundLine 4, 'f2', SHOW
		linesCollector.storeContextLine 'c3'

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
}
