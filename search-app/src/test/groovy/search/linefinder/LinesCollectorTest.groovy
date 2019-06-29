package search.linefinder

import static search.linefinder.LineVisibility.HIDE
import static search.linefinder.LineVisibility.SHOW

import org.junit.Test
import search.linefinder.LinesCollector

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
		// given
		linesCollector = new LinesCollector(0, 0, 100)

		// when
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE
		linesCollector.storeFoundLine TEST_LINENR, TEST_LINE, SHOW
		linesCollector.storeContextLine TEST_LINE_CONTEXT_AFTER

		// then
		assert !linesCollector.foundLines.size()
		assert !linesCollector.currentContextLinesBefore
	}

	@Test
	void noMatchedLinesAndOneContextLineEnabled_singleMatch() {
		// given
		linesCollector = new LinesCollector(0, 1, 100)

		// when
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE
		linesCollector.storeFoundLine TEST_LINENR, TEST_LINE, SHOW
		linesCollector.storeContextLine TEST_LINE_CONTEXT_AFTER

		// then
		assert !linesCollector.foundLines.size()
		assert !linesCollector.currentContextLinesBefore
	}

	@Test
	void oneMatchedLineAndNoContextEnabled_shouldYieldOneLine() {
		// given
		linesCollector = new LinesCollector(1, 0, 100)

		// when
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE
		linesCollector.storeFoundLine TEST_LINENR, TEST_LINE, SHOW
		linesCollector.storeContextLine TEST_LINE_CONTEXT_AFTER

		// then
		assert linesCollector.foundLines.size() == 1
		linesCollector.foundLines[0].with {
			assert line == TEST_LINE
			assert lineNr == TEST_LINENR
			assert contextLinesBefore == []
			assert contextLinesAfter == []
			assert !contextLinesBeforeOverflow
			assert !contextLinesAfterOverflow
		}
		assert !linesCollector.currentContextLinesBefore
	}

	@Test
	void oneMatchedLineAndOneContextLineEnabled_singleMatch_withoutOverflow() {
		// given
		linesCollector = new LinesCollector(1, 1, 100)

		// when
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE
		linesCollector.storeFoundLine TEST_LINENR, TEST_LINE, SHOW
		linesCollector.storeContextLine TEST_LINE_CONTEXT_AFTER

		// then
		assert linesCollector.foundLines.size() == 1
		linesCollector.foundLines[0].with {
			assert line == TEST_LINE
			assert lineNr == TEST_LINENR
			assert contextLinesBefore == [TEST_LINE_CONTEXT_BEFORE]
			assert contextLinesAfter == [TEST_LINE_CONTEXT_AFTER]
			assert !contextLinesBeforeOverflow
			assert !contextLinesAfterOverflow
		}
		assert !linesCollector.currentContextLinesBefore
	}

	@Test
	void oneMatchedLineAndOneContextLineEnabled_singleMatch_withOverflow() {
		// given
		linesCollector = new LinesCollector(1, 1, 100)

		// when
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE_BEFORE
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE
		linesCollector.storeFoundLine TEST_LINENR, TEST_LINE, SHOW
		linesCollector.storeContextLine TEST_LINE_CONTEXT_AFTER
		linesCollector.storeContextLine TEST_LINE_CONTEXT_AFTER_AFTER

		// then
		assert linesCollector.foundLines.size() == 1
		linesCollector.foundLines[0].with {
			assert line == TEST_LINE
			assert lineNr == TEST_LINENR
			assert contextLinesBefore == [TEST_LINE_CONTEXT_BEFORE]
			assert contextLinesAfter == [TEST_LINE_CONTEXT_AFTER]
			assert contextLinesBeforeOverflow
			assert contextLinesAfterOverflow
		}
		assert !linesCollector.currentContextLinesBefore
	}

	@Test
	void oneMatchedLineAndOneContextLineEnabled_reset() {
		// given
		linesCollector = new LinesCollector(1, 1, 100)

		// when
		linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE
		linesCollector.storeFoundLine TEST_LINENR, TEST_LINE, SHOW
		linesCollector.storeContextLine TEST_LINE_CONTEXT_AFTER
		linesCollector.reset()

		// then
		assert linesCollector.foundLines.size() == 0
		assert !linesCollector.currentContextLinesBefore
	}

	@Test
	void oneMatchedLineAndOneContextLineEnabled_twoMatches() {
		// given
		linesCollector = new LinesCollector(1, 1, 100)

		// when
		linesCollector.storeContextLine 'c1'
		linesCollector.storeFoundLine 2, 'f1', SHOW
		linesCollector.storeContextLine 'c2'
		linesCollector.storeFoundLine 4, 'f2', SHOW
		linesCollector.storeContextLine 'c3'

		// then
		assert linesCollector.foundLines.size() == 2
		linesCollector.foundLines[0].with {
			assert line == 'f1'
			assert lineNr == 2
			assert contextLinesBefore == ['c1']
			assert contextLinesAfter == ['c2']
			assert !contextLinesBeforeOverflow
			assert !contextLinesAfterOverflow
		}
		linesCollector.foundLines[1].with {
			assert line == ''
			assert lineNr == -1
			assert contextLinesBefore == []
			assert contextLinesAfter == []
			assert !contextLinesBeforeOverflow
			assert !contextLinesAfterOverflow
		}
		assert !linesCollector.currentContextLinesBefore
	}

	@Test
	void oneMatchedLineAndOneContextLineEnabled_oneMatch_hidden() {
		// given
		linesCollector = new LinesCollector(1, 1, 100)

		// when
		linesCollector.storeContextLine 'c1'
		linesCollector.storeFoundLine 2, 'f1', HIDE
		linesCollector.storeContextLine 'c2'

		assert !linesCollector.foundLines.size()
	}

	@Test
	void oneMatchedLineAndOneContextLineEnabled_twoMatches_oneHidden() {
		// given
		linesCollector = new LinesCollector(1, 1, 100)

		// when
		linesCollector.storeContextLine 'c1'
		linesCollector.storeFoundLine 2, 'f1', SHOW
		linesCollector.storeContextLine 'c2'
		linesCollector.storeFoundLine 4, 'f2', HIDE
		linesCollector.storeContextLine 'c3'

		// then
		assert linesCollector.foundLines.size() == 1
		linesCollector.foundLines[0].with {
			assert line == 'f1'
			assert lineNr == 2
			assert contextLinesBefore == ['c1']
			assert contextLinesAfter == ['c2']
			assert !contextLinesBeforeOverflow
			assert contextLinesAfterOverflow
		}
		assert !linesCollector.currentContextLinesBefore
	}

	@Test
	void twoMatchedLinesAndOneContextLineEnabled() {
		// given
		linesCollector = new LinesCollector(2, 1, 100)

		// when
		linesCollector.storeContextLine 'c1'
		linesCollector.storeFoundLine 2, 'f1', SHOW
		linesCollector.storeContextLine 'c2'
		linesCollector.storeFoundLine 4, 'f2', SHOW
		linesCollector.storeContextLine 'c3'

		// then
		assert linesCollector.foundLines.size() == 2
		linesCollector.foundLines[0].with {
			assert line == 'f1'
			assert lineNr == 2
			assert contextLinesBefore == ['c1']
			assert contextLinesAfter == ['c2']
			assert !contextLinesBeforeOverflow
			assert !contextLinesAfterOverflow
		}
		linesCollector.foundLines[1].with {
			assert line == 'f2'
			assert lineNr == 4
			assert contextLinesBefore == []
			assert contextLinesAfter == ['c3']
			assert !contextLinesBeforeOverflow
			assert !contextLinesAfterOverflow
		}
		assert !linesCollector.currentContextLinesBefore
	}
}
