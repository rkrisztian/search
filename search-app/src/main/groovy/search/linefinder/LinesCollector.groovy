package search.linefinder

import static LineVisibility.SHOW

import groovy.transform.CompileStatic
import search.annotations.VisibleForTesting

/**
 * Collects matched lines with context for displaying search results later.
 */
@CompileStatic
class LinesCollector implements ILinesCollector {

	private final Integer maxContextLines
	private final int maxMatchedLinesPerFile
	private final int maxDisplayedLineLength

	private LinkedList<FoundLine> foundLines
	private boolean initialized
	@VisibleForTesting protected LinkedList<String> currentContextLinesBefore
	private boolean currentContextLinesBeforeOverflow

	LinesCollector(int maxMatchedLinesPerFile, Integer maxContextLines, int maxDisplayedLineLength) {
		this.maxContextLines = maxContextLines
		this.maxMatchedLinesPerFile = maxMatchedLinesPerFile
		this.maxDisplayedLineLength = maxDisplayedLineLength

		reset()
	}

	void reset() {
		foundLines = new LinkedList<FoundLine>()
		initialized = false
		currentContextLinesBefore = null
		currentContextLinesBeforeOverflow = false
	}

	void storeFoundLine(int lineNr, String line, LineVisibility lineVisibility) {
		lazyInit()

		if (lineVisibility == SHOW) {
			if (canDisplayMoreFoundLines()) {
				// If line too long, truncate it before printing
				if ((maxDisplayedLineLength > 0) && (line.size() > maxDisplayedLineLength)) {
					line = "${line[0..maxDisplayedLineLength - 1]}..."
				}

				foundLines.add makeFoundLineWithContextLinesBefore(lineNr, line)
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
				contextLinesBeforeOverflow: currentContextLinesBeforeOverflow,
				contextLinesAfter: new LinkedList<String>(), contextLinesAfterOverflow: false)
	}

	private FoundLine makeSkippedLinesMarker() {
		new FoundLine(
				lineNr: -1, line: '', contextLinesBefore: new LinkedList<String>(),
				contextLinesBeforeOverflow: false,
				contextLinesAfter: new LinkedList<String>(), contextLinesAfterOverflow: false)
	}

	void storeContextLine(String line) {
		lazyInit()

		if (!maxMatchedLinesPerFile || !maxContextLines) {
			return
		}
		if (hasDisplayedSkippedLinesMarker()) {
			return
		}

		boolean addedToContextLinesAfter = false

		if (foundLines) {
			addedToContextLinesAfter = addToContextLinesAfterIfNotOverflow line, foundLines.last
		}

		if (!addedToContextLinesAfter && !hasPreviouslyFoundTheLastDisplayableLine()) {
			addToContextLinesBefore line
		}
	}

	private void lazyInit() {
		if (initialized) {
			return
		}

		resetContextLinesBefore()
		initialized = true
	}

	private void resetContextLinesBefore() {
		currentContextLinesBefore = new LinkedList<String>()
		currentContextLinesBeforeOverflow = false
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
			currentContextLinesBefore.removeFirst()
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
