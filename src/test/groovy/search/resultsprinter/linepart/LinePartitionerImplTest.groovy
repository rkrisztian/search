package search.resultsprinter.linepart

import static search.colors.ColorType.DRY_REPLACE_COLOR
import static search.colors.ColorType.MATCH_COLOR
import static search.colors.ColorType.REPLACE_COLOR
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_COLORS
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_DRY_RUN
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_REPLACE
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.WITH_COLORS
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.WITH_DRY_RUN
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.WITH_REPLACE

import search.conf.PatternData
import spock.lang.Specification

class LinePartitionerImplTest extends Specification {

	void 'should partition empty line'() {
		given:
			def partitioner = new LinePartitionerImpl([new PatternData(searchPattern: ~/dummyPattern/)] as Set,
					NO_REPLACE, NO_DRY_RUN, WITH_COLORS)

		expect:
			partitioner.partition('') == []
	}

	void 'should create right partitions, without replace [pattern: #patternData.searchPattern, colors: #disableColors]'(
			Set<PatternData> patternData, boolean disableColors, List<? extends LinePart> expectedLineParts) {
		given:
			def partitioner = new LinePartitionerImpl(patternData, NO_REPLACE, NO_DRY_RUN, disableColors)

		expect:
			partitioner.partition('this is a test') == expectedLineParts

		where:
			[patternData, disableColors, expectedLineParts] << [
					[
							[new PatternData(searchPattern: ~/this/, hidePattern: true)] as Set,
							WITH_COLORS,
							[new LinePart(text: 'this is a test')]
					],
					[
							[new PatternData(searchPattern: ~/this will not match/)] as Set,
							WITH_COLORS,
							[new LinePart(text: 'this is a test')]
					],
					[
							[new PatternData(searchPattern: ~/\s+is\s+a\s+/)] as Set,
							WITH_COLORS,
							[
									new LinePart(text: 'this'),
									new LinePart(text: ' is a ', colorType: MATCH_COLOR),
									new LinePart(text: 'test')
							]
					],
					[
							[new PatternData(searchPattern: ~/\s+is\s+a\s+/)] as Set,
							NO_COLORS,
							[new LinePart(text: 'this is a test')]
					],
					[
							[new PatternData(searchPattern: ~/t/)] as Set,
							WITH_COLORS,
							[
									new LinePart(text: 't', colorType: MATCH_COLOR),
									new LinePart(text: 'his is a '),
									new LinePart(text: 't', colorType: MATCH_COLOR),
									new LinePart(text: 'es'),
									new LinePart(text: 't', colorType: MATCH_COLOR)
							]
					],
					[
							[
									new PatternData(searchPattern: ~/this/),
									new PatternData(searchPattern: ~/a/)
							] as Set,
							WITH_COLORS,
							[
									new LinePart(text: 'this', colorType: MATCH_COLOR),
									new LinePart(text: ' is '),
									new LinePart(text: 'a', colorType: MATCH_COLOR),
									new LinePart(text: ' test')
							]
					]
			]
	}

	void 'should create right partitions, with replace [pattern: #patternData.searchPattern, colors: #disableColors]'(
			Set<PatternData> patternData, boolean disableColors, List<? extends LinePart> expectedLineParts) {
		given:
			def partitioner = new LinePartitionerImpl(patternData, WITH_REPLACE, NO_DRY_RUN, disableColors)

		expect:
			partitioner.partition('this is a test') == expectedLineParts

		where:
			[patternData, disableColors, expectedLineParts] << [
					[
							[
									new PatternData(searchPattern: ~/is a test/),
									new PatternData(searchPattern: ~/a test/, replace: true, replaceText: 'A TEST')
							] as Set,
							WITH_COLORS,
							[
									new LinePart(text: 'this '),
									new LinePart(text: 'is ', colorType: MATCH_COLOR),
									new LinePart(text: 'A TEST', colorType: REPLACE_COLOR)
							]
					],
					[
							[
									new PatternData(searchPattern: ~/is a test/),
									new PatternData(searchPattern: ~/a test/, replace: true, replaceText: 'A TEST')
							] as Set,
							NO_COLORS,
							[new LinePart(text: 'this is A TEST')]
					],
					[
							[new PatternData(searchPattern: ~/is/, replace: true, replaceText: 'IISS')] as Set,
							WITH_COLORS,
							[
									new LinePart(text: 'th'),
									new LinePart(text: 'IISS', colorType: REPLACE_COLOR),
									new LinePart(text: ' '),
									new LinePart(text: 'IISS', colorType: REPLACE_COLOR),
									new LinePart(text: ' a test')
							]
					]
			]
	}

	void 'should create right partitions, with dry replace [pattern: #patternData.searchPattern, colors: #disableColors]'(
			Set<PatternData> patternData, boolean disableColors, List<? extends LinePart> expectedLineParts) {
		given:
			def partitioner = new LinePartitionerImpl(patternData, WITH_REPLACE, WITH_DRY_RUN, disableColors)

		expect:
			partitioner.partition('this is a test') == expectedLineParts

		where:
			[patternData, disableColors, expectedLineParts] << [
					[
							[new PatternData(searchPattern: ~/a test/, replace: true, replaceText: 'A TEST')] as Set,
							WITH_COLORS,
							[
									new LinePart(text: 'this is '),
									new LinePart(text: 'A TEST', colorType: DRY_REPLACE_COLOR)
							]
					],
					[
							[new PatternData(searchPattern: ~/a test/, replace: true, replaceText: 'A TEST')] as Set,
							NO_COLORS,
							[new LinePart(text: 'this is A TEST')]
					]
			]
	}

}
