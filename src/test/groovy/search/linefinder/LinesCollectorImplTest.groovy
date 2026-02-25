package search.linefinder

import static search.linefinder.LineVisibility.HIDE
import static search.linefinder.LineVisibility.SHOW

import spock.lang.Specification

class LinesCollectorImplTest extends Specification {

	private static final String TEST_LINE_CONTEXT_BEFORE_BEFORE = 'context before before'
	private static final String TEST_LINE_CONTEXT_BEFORE = 'context before'
	private static final String TEST_LINE = 'test'
	private static final int TEST_LINENR = 3
	private static final String TEST_LINE_CONTEXT_AFTER = 'context after'
	private static final String TEST_LINE_CONTEXT_AFTER_AFTER = 'context after after'

	private LinesCollectorImpl linesCollector

	void 'matched lines and context disabled, found one matched line, should yield empty list'() {
		given:
			linesCollector = new LinesCollectorImpl(0, 0, 100)

		when:
			linesCollector.storeFoundLine TEST_LINENR, TEST_LINE, SHOW

		then:
			verifyAll(linesCollector) {
				!foundLines
				!currentContextLinesBefore
			}
	}

	void 'matched lines and context disabled, found one context line, should yield empty list'() {
		given:
			linesCollector = new LinesCollectorImpl(0, 0, 100)

		when:
			linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE

		then:
			verifyAll(linesCollector) {
				!foundLines
				!currentContextLinesBefore
			}
	}

	void 'no matched lines and one context line enabled, should yield empty list'() {
		given:
			linesCollector = new LinesCollectorImpl(0, 1, 100)

		when:
			linesCollector.storeContextLine TEST_LINE_CONTEXT_BEFORE

		then:
			verifyAll(linesCollector) {
				!foundLines
				!currentContextLinesBefore
			}
	}

	void 'one matched line and no context enabled, should yield one line'() {
		given:
			linesCollector = new LinesCollectorImpl(1, 0, 100)

		when:
			linesCollector.with {
				storeContextLine TEST_LINE_CONTEXT_BEFORE
				storeFoundLine TEST_LINENR, TEST_LINE, SHOW
				storeContextLine TEST_LINE_CONTEXT_AFTER
			}

		then:
			verifyAll(linesCollector) {
				foundLines == [new FoundLine(line: TEST_LINE, lineNr: TEST_LINENR)]
				!currentContextLinesBefore
			}
	}

	void 'one matched long line, should yield one truncated line'() {
		given:
			linesCollector = new LinesCollectorImpl(1, 0, 20)

		when:
			linesCollector.storeFoundLine TEST_LINENR, 'this is a very long line that should be truncated', SHOW

		then:
			linesCollector.foundLines == [new FoundLine(line: 'this is a very long ...', lineNr: TEST_LINENR)]
	}

	void 'one matched line and one context line enabled, single match, without overflow'() {
		given:
			linesCollector = new LinesCollectorImpl(1, 1, 100)

		when:
			linesCollector.with {
				storeContextLine TEST_LINE_CONTEXT_BEFORE
				storeFoundLine TEST_LINENR, TEST_LINE, SHOW
				storeContextLine TEST_LINE_CONTEXT_AFTER
			}

		then:
			verifyAll(linesCollector) {
				foundLines == [new FoundLine(
						line: TEST_LINE,
						lineNr: TEST_LINENR,
						contextLinesBefore: [TEST_LINE_CONTEXT_BEFORE],
						contextLinesAfter: [TEST_LINE_CONTEXT_AFTER]
				)]
				!currentContextLinesBefore
			}
	}

	void 'one matched line and one context line enabled, single match, with overflow'() {
		given:
			linesCollector = new LinesCollectorImpl(1, 1, 100)

		when:
			linesCollector.with {
				storeContextLine TEST_LINE_CONTEXT_BEFORE_BEFORE
				storeContextLine TEST_LINE_CONTEXT_BEFORE
				storeFoundLine TEST_LINENR, TEST_LINE, SHOW
				storeContextLine TEST_LINE_CONTEXT_AFTER
				storeContextLine TEST_LINE_CONTEXT_AFTER_AFTER
			}

		then:
			verifyAll(linesCollector) {
				foundLines == [new FoundLine(
						line: TEST_LINE,
						lineNr: TEST_LINENR,
						contextLinesBefore: [TEST_LINE_CONTEXT_BEFORE],
						contextLinesAfter: [TEST_LINE_CONTEXT_AFTER],
						contextLinesBeforeOverflow: true,
						contextLinesAfterOverflow: true
				)]
				!currentContextLinesBefore
			}
	}

	void 'one matched line and one context line enabled, reset'() {
		given:
			linesCollector = new LinesCollectorImpl(1, 1, 100)

		when:
			linesCollector.with {
				storeContextLine TEST_LINE_CONTEXT_BEFORE
				storeFoundLine TEST_LINENR, TEST_LINE, SHOW
				storeContextLine TEST_LINE_CONTEXT_AFTER
				reset()
			}

		then:
			verifyAll(linesCollector) {
				!foundLines
				!currentContextLinesBefore
			}
	}

	void 'one matched line and one context line enabled, one match displayed'() {
		given:
			linesCollector = new LinesCollectorImpl(1, 1, 100)

		when:
			linesCollector.with {
				storeContextLine 'c1'
				storeFoundLine 2, 'f1', SHOW
				storeContextLine 'c2'
				storeFoundLine 4, 'f2', SHOW
			}

		then:
			verifyAll(linesCollector) {
				hasFinished()
				foundLines == [
						new FoundLine(
								line: 'f1',
								lineNr: 2,
								contextLinesBefore: ['c1'],
								contextLinesAfter: ['c2'],
								contextLinesAfterOverflow: true
						)
				]
				!currentContextLinesBefore
			}
	}

	void 'one matched line and one context line enabled, one match, hidden'() {
		given:
			linesCollector = new LinesCollectorImpl(1, 1, 100)

		when:
			linesCollector.with {
				storeContextLine 'c1'
				storeFoundLine 2, 'f1', HIDE
				storeContextLine 'c2'
			}

		then:
			!linesCollector.foundLines
	}

	void 'one matched line and one context line enabled, two matches, one hidden'() {
		given:
			linesCollector = new LinesCollectorImpl(1, 1, 100)

		when:
			linesCollector.with {
				storeContextLine 'c1'
				storeFoundLine 2, 'f1', SHOW
				storeContextLine 'c2'
				storeFoundLine 4, 'f2', HIDE
				storeContextLine 'c3'
			}

		then:
			verifyAll(linesCollector) {
				foundLines == [new FoundLine(
						line: 'f1',
						lineNr: 2,
						contextLinesBefore: ['c1'],
						contextLinesAfter: ['c2'],
						contextLinesAfterOverflow: true
				)]
				!currentContextLinesBefore
			}
	}

	void 'two matched lines and one context line enabled'() {
		given:
			linesCollector = new LinesCollectorImpl(2, 1, 100)

		when:
			linesCollector.with {
				storeContextLine 'c1'
				storeFoundLine 2, 'f1', SHOW
				storeContextLine 'c2'
				storeFoundLine 4, 'f2', SHOW
				storeContextLine 'c3'
			}

		then:
			verifyAll(linesCollector) {
				foundLines == [
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
				!currentContextLinesBefore
			}
	}

	void 'matched line should be displayed as context line after the limit, mixed'() {
		given:
			linesCollector = new LinesCollectorImpl(1, 2, 100)

		when:
			linesCollector.with {
				storeContextLine 'c1'
				storeFoundLine 2, 'f1', SHOW
				storeContextLine 'c2'
				storeFoundLine 4, 'f2', SHOW
				storeContextLine 'c3'
			}

		then:
			linesCollector.foundLines == [
					new FoundLine(
							line: 'f1',
							lineNr: 2,
							contextLinesBefore: ['c1'],
							contextLinesAfter: ['c2', 'f2'],
							contextLinesAfterOverflow: true
					)
			]
	}

	void 'matched line should be displayed as context line after the limit, only found lines'() {
		given:
			linesCollector = new LinesCollectorImpl(1, 2, 100)

		when:
			linesCollector.with {
				storeFoundLine 2, 'f1', SHOW
				storeFoundLine 3, 'f2', SHOW
				storeFoundLine 4, 'f3', SHOW
			}

		then:
			linesCollector.foundLines == [
					new FoundLine(
							line: 'f1',
							lineNr: 2,
							contextLinesAfter: ['f2', 'f3'],
							contextLinesAfterOverflow: false
					)
			]
	}

	void 'unlimited matches and one context line enabled'() {
		given:
			linesCollector = new LinesCollectorImpl(-1, 1, 100)

		when:
			linesCollector.with {
				storeContextLine 'c1'
				storeFoundLine 2, 'f1', SHOW
				storeContextLine 'c2'
				storeFoundLine 4, 'f2', SHOW
				storeContextLine 'c3'
				storeContextLine 'c4'
			}

		then:
			verifyAll(linesCollector) {
				foundLines == [
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
				currentContextLinesBefore
			}
	}

	void 'should not be overflow between just enough context lines'() {
		given:
			linesCollector = new LinesCollectorImpl(-1, 2, 100)

		when:
			linesCollector.with {
				storeFoundLine 1, 'f1', SHOW
				storeContextLine 'c1'
				storeContextLine 'c2'
				storeContextLine 'c3'
				storeContextLine 'c4'
				storeFoundLine 6, 'f2', SHOW
			}

		then:
			linesCollector.foundLines == [
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

	void 'should overflow before and after with too many context lines'() {
		given:
			linesCollector = new LinesCollectorImpl(-1, 2, 100)

		when:
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

		then:
			linesCollector.foundLines == [
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
							contextLinesAfter: []
					)
			]
	}

	void 'do not accept found lines when search finished'() {
		given:
			linesCollector = new LinesCollectorImpl(1, 1, 100)
			linesCollector.with {
				storeContextLine 'c1'
				storeFoundLine 2, 'f1', SHOW
				storeContextLine 'c2'
				storeFoundLine 4, 'f2', SHOW
			}

		when:
			linesCollector.storeFoundLine 5, 'f3', SHOW

		then:
			thrown LinesCollectorImpl.LinesCollectorException
	}

	void 'do not accept context lines when search finished'() {
		given:
			linesCollector = new LinesCollectorImpl(1, 1, 100)
			linesCollector.with {
				storeContextLine 'c1'
				storeFoundLine 2, 'f1', SHOW
				storeContextLine 'c2'
				storeFoundLine 4, 'f2', SHOW
			}

		when:
			linesCollector.storeContextLine 'c3'

		then:
			thrown LinesCollectorImpl.LinesCollectorException
	}

}
