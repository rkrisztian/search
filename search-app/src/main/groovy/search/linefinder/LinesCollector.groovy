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
	}

	private void resetContextLinesBefore() {
		currentContextLinesBefore = [] as LinkedList<String>
		currentContextLinesBeforeOverflow = false
	}

	void storeFoundLine(int lineNr, String line, LineVisibility lineVisibility) {
		if (lineVisibility == SHOW) {
			if (canDisplayMoreFoundLines()) {
				String truncatedLine = ((maxDisplayedLineLength > 0) && (line.size() > maxDisplayedLineLength))
						? "${line[0..maxDisplayedLineLength - 1]}..."
						: line

				foundLines.add makeFoundLineWithContextLinesBefore(lineNr, truncatedLine)
			}
			else if (hasPreviouslyFoundTheLastDisplayableLine()) {
				foundLines.add makeSkippedLinesMarker()
			}
		}

		resetContextLinesBefore()
	}

	private FoundLine makeFoundLineWithContextLinesBefore(int lineNr, String line) {
		new FoundLine(
				lineNr: lineNr, line: line, contextLinesBefore: currentContextLinesBefore,
				contextLinesBeforeOverflow: currentContextLinesBeforeOverflow)
	}

	private static FoundLine makeSkippedLinesMarker() {
		new FoundLine(lineNr: -1, line: '')
	}

	void storeContextLine(String line) {
		if (!maxMatchedLinesPerFile || !maxContextLines) {
			return
		}
		if (hasDisplayedSkippedLinesMarker()) {
			return
		}

		boolean addedToContextLinesAfter = false

		if (foundLines) {
			addedToContextLinesAfter = addToContextLinesAfterIfNotOverflow line, foundLines.last()
		}

		if (!addedToContextLinesAfter && !hasPreviouslyFoundTheLastDisplayableLine()) {
			addToContextLinesBefore line
		}
	}

	private boolean addToContextLinesAfterIfNotOverflow(String line, FoundLine foundLine) {
		if (foundLine.contextLinesAfter.size() < maxContextLines) {
			foundLine.contextLinesAfter.add line
			foundLine.contextLinesAfterOverflow = false
			return true
		}

		foundLine.contextLinesAfterOverflow = true
		false
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

	private boolean hasPreviouslyFoundTheLastDisplayableLine() {
		(maxMatchedLinesPerFile > 0) && (foundLines.size() == maxMatchedLinesPerFile)
	}

	private boolean hasDisplayedSkippedLinesMarker() {
		(maxMatchedLinesPerFile > 0) && (foundLines.size() > maxMatchedLinesPerFile)
	}

	List<FoundLine> getFoundLines() {
		foundLines
	}

}
