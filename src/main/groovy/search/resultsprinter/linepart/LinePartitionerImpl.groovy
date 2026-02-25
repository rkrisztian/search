package search.resultsprinter.linepart

import static search.colors.ColorType.DRY_REPLACE_COLOR
import static search.colors.ColorType.MATCH_COLOR
import static search.colors.ColorType.REPLACE_COLOR

import groovy.transform.CompileStatic
import search.colors.ColorType
import search.conf.PatternData

/**
 * Helps coloring matched/replaced parts of a line.
 */
@CompileStatic
class LinePartitionerImpl implements LinePartitioner {

	private final Set<PatternData> searchPatternData
	private final Set<PatternData> replacePatternData

	private final boolean doReplace

	private final boolean disableColors

	private final ColorType replaceColor

	LinePartitionerImpl(Set<PatternData> patternData, boolean doReplace, boolean dryRun, boolean disableColors) {
		this.searchPatternData = patternData.findAll { !it.hidePattern && !it.replace }
		this.replacePatternData = patternData.findAll { !it.hidePattern && it.replace }

		this.doReplace = doReplace
		this.disableColors = disableColors
		replaceColor = dryRun ? DRY_REPLACE_COLOR : REPLACE_COLOR
	}

	@Override
	List<LinePart> partition(String originalLine) {
		def lc = new LineWithColorMap(
				line: originalLine,
				colorMap: [null as ColorType] * originalLine.size()
		)

		if (!disableColors) {
			colorizeSearches lc
		}
		if (doReplace) {
			doReplacesAndColorize lc
		}

		(disableColors) ?
				[new LinePart(text: lc.line)] :
				breakIntoLineParts(lc)
	}

	private void colorizeSearches(LineWithColorMap lc) {
		lc.with {
			searchPatternData.each { patternData ->
				def m = line =~ patternData.searchPattern

				while (m.find()) {
					colorMap = replaceSublist colorMap, m.start(), m.end(), [MATCH_COLOR] * (m.end() - m.start())
				}
			}
		}
	}

	private void doReplacesAndColorize(LineWithColorMap lc) {
		lc.with {
			replacePatternData.each { patternData ->
				def m = line =~ patternData.searchPattern
				def offset = 0

				while (m.find()) {
					def newStart = m.start() + offset
					def newEnd = m.end() + offset
					offset += patternData.replaceText.size() - (newEnd - newStart)

					line = replaceSubstring line, newStart, newEnd, patternData.replaceText

					if (!disableColors) {
						colorMap = replaceSublist colorMap, newStart, newEnd, [replaceColor] * patternData.replaceText.size()
					}
				}
			}
		}
	}

	private static String replaceSubstring(String line, int start, int end, String replaceText) {
		(start > 0 ? line[0..<start] : '') + replaceText + (end < line.size() ? line[end..-1] : '')
	}

	private static List<ColorType> replaceSublist(List<ColorType> list, int start, int end, List<ColorType> replaceColors) {
		(start > 0 ? list[0..<start] : []) + replaceColors + (end < list.size() ? list[end..-1] : [])
	}

	private static List<LinePart> breakIntoLineParts(LineWithColorMap lc) {
		List<LinePart> lineParts = []

		lc.with {
			// TODO: Ugly loop
			def partStart = 0
			def partLength = 0

			for (int i = 0; i < colorMap.size(); i++) {
				if (colorMap[i] == colorMap[partStart]) {
					partLength++
				}
				else {
					lineParts += new LinePart(text: line[partStart..<(partStart + partLength)], colorType: colorMap[i - 1])
					partStart = i
					partLength = 1
				}
			}
			if (partLength) {
				lineParts += new LinePart(text: line[partStart..<(partStart + partLength)], colorType: colorMap[-1])
			}
		}

		lineParts
	}

}
