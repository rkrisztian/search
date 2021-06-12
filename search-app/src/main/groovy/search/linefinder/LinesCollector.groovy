package search.linefinder

import static LineVisibility.SHOW

import groovy.transform.CompileStatic
import search.annotations.VisibleForTesting

/**
 * Collects {@link FoundLine}s for displaying search results later.
 */
@CompileStatic
class LinesCollector implements ILinesCollector {

	private final Integer maxContextLines
	private final int maxMatchedLinesPerFile
	private final int maxDisplayedLineLength

	private List<FoundLine> foundLines
	private boolean finished

	@VisibleForTesting
	protected List<String> currentContextLinesBefore

	private boolean currentContextLinesBeforeOverflow

	LinesCollector(int maxMatchedLinesPerFile, Integer maxContextLines, int maxDisplayedLineLength) {
		this.maxContextLines = maxContextLines
		this.maxMatchedLinesPerFile = maxMatchedLinesPerFile
		this.maxDisplayedLineLength = maxDisplayedLineLength

		reset()
	}

	void reset() {
		foundLines = [] as LinkedList<FoundLine>
		resetContextLinesBefore()
		finished = false
	}

	private void resetContextLinesBefore() {
		currentContextLinesBefore = [] as LinkedList<String>
		currentContextLinesBeforeOverflow = false
	}

	void storeFoundLine(int lineNr, String line, LineVisibility lineVisibility) {
		if (finished) {
			throw new LinesCollectorException('Search finished')
		}

		if (!maxMatchedLinesPerFile) {
			finished = true
			return
		}

		if (lineVisibility == SHOW) {
			if (canDisplayMoreFoundLines()) {
				String truncatedLine = ((maxDisplayedLineLength > 0) && (line.size() > maxDisplayedLineLength))
						? "${line[0..maxDisplayedLineLength - 1]}..."
						: line

				foundLines.add makeFoundLineWithContextLinesBefore(lineNr, truncatedLine)
				adjustPreviousContextLinesAfterIfDidNotOverflow()
			}
			else {
				addToContextLinesAfterIfNotOverflow line
			}
		}

		resetContextLinesBefore()
	}

	private FoundLine makeFoundLineWithContextLinesBefore(int lineNr, String line) {
		new FoundLine(
				lineNr: lineNr, line: line, contextLinesBefore: currentContextLinesBefore,
				contextLinesBeforeOverflow: currentContextLinesBeforeOverflow)
	}

	private void adjustPreviousContextLinesAfterIfDidNotOverflow() {
		if (foundLines.size() < 2) {
			return
		}

		if (!foundLines[-1].contextLinesBeforeOverflow && foundLines[-2].contextLinesAfterOverflow) {
			foundLines[-2].contextLinesAfterOverflow = false
		}
	}

	void storeContextLine(String line) {
		if (finished) {
			throw new LinesCollectorException('Search finished')
		}

		if (!maxMatchedLinesPerFile) {
			finished = true
			return
		}

		if (!maxContextLines) {
			if (!canDisplayMoreFoundLines()) {
				finished = true
			}

			return
		}

		if (foundLines) {
			addToContextLinesAfterIfNotOverflow line

			if (foundLines.last().contextLinesAfterOverflow && finished) {
				return
			}
		}

		if (!foundLines || (foundLines.last().contextLinesAfterOverflow && canDisplayMoreFoundLines())) {
			addToContextLinesBefore line
		}
	}

	private void addToContextLinesAfterIfNotOverflow(String line) {
		def foundLine = foundLines.last()

		if (foundLine.contextLinesAfter.size() < maxContextLines) {
			foundLine.contextLinesAfter.add line
			foundLine.contextLinesAfterOverflow = false
		}
		else {
			foundLine.contextLinesAfterOverflow = true

			if (!canDisplayMoreFoundLines()) {
				finished = true
			}
		}
	}

	private void addToContextLinesBefore(String line) {
		currentContextLinesBefore.add line

		if (currentContextLinesBefore.size() <= maxContextLines) {
			currentContextLinesBeforeOverflow = false
		}
		else {
			currentContextLinesBefore.pop()
			currentContextLinesBeforeOverflow = true
		}
	}

	private boolean canDisplayMoreFoundLines() {
		(maxMatchedLinesPerFile < 0) || (foundLines.size() < maxMatchedLinesPerFile)
	}

	List<FoundLine> getFoundLines() {
		foundLines
	}

	boolean hasFinished() {
		finished
	}

	static class LinesCollectorException extends RuntimeException {

		LinesCollectorException(String message) {
			super(message)
		}

	}

}
