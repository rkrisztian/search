package search.resultsprinter.linepart


import static search.colors.ColorType.DRY_REPLACE_COLOR
import static search.colors.ColorType.MATCH_COLOR
import static search.colors.ColorType.REPLACE_COLOR
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.NO_COLORS
import static search.resultsprinter.testutil.ResultsPrinterTestConstants.WITH_COLORS

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import search.conf.PatternData

import java.util.stream.Stream

class LinePartitionerTest {

	@ParameterizedTest
	@MethodSource('shouldCreateRightSearchPartitionsArgs')
	void shouldCreateRightSearchPartitions(Set<PatternData> patternData, List<? extends LinePart> expectedLineParts,
			boolean disableColors) {
		def partitioner = new LinePartitioner(patternData, false, false, disableColors)
		assert partitioner.partition('this is a test') == expectedLineParts
	}

	static Stream<Arguments> shouldCreateRightSearchPartitionsArgs() {
		Stream.of(
				Arguments.of(
						[new PatternData(searchPattern: ~/this/, hidePattern: true)] as Set,
						[new LinePart(text: 'this is a test', colorType: null)],
						WITH_COLORS
				),

				Arguments.of(
						[new PatternData(searchPattern: ~/this will not match/)] as Set,
						[new LinePart(text: 'this is a test', colorType: null)],
						WITH_COLORS
				),

				Arguments.of(
						[new PatternData(searchPattern: ~/\s+is\s+a\s+/)] as Set,
						[
								new LinePart(text: 'this', colorType: null),
								new LinePart(text: ' is a ', colorType: MATCH_COLOR),
								new LinePart(text: 'test', colorType: null)
						],
						WITH_COLORS
				),

				Arguments.of(
						[new PatternData(searchPattern: ~/\s+is\s+a\s+/)] as Set,
						[new LinePart(text: 'this is a test', colorType: null)],
						NO_COLORS
				)
		)
	}

	@ParameterizedTest
	@MethodSource('shouldCreateRightPartitionsWithReplaceArgs')
	void shouldCreateRightPartitionsWithReplace(Set<PatternData> patternData, List<? extends LinePart> expectedLineParts,
			boolean disableColors) {
		def partitioner = new LinePartitioner(patternData, true, false, disableColors)
		assert partitioner.partition('this is a test') == expectedLineParts
	}

	static Stream<Arguments> shouldCreateRightPartitionsWithReplaceArgs() {
		Stream.of(
				Arguments.of(
						[
								new PatternData(searchPattern: ~/is a test/),
								new PatternData(searchPattern: ~/a test/, replace: true, replaceText: 'A TEST')
						] as Set,
						[
								new LinePart(text: 'this ', colorType: null),
								new LinePart(text: 'is ', colorType: MATCH_COLOR),
								new LinePart(text: 'A TEST', colorType: REPLACE_COLOR)
						],
						WITH_COLORS
				),

				Arguments.of(
						[
								new PatternData(searchPattern: ~/is a test/),
								new PatternData(searchPattern: ~/a test/, replace: true, replaceText: 'A TEST')
						] as Set,
						[new LinePart(text: 'this is A TEST', colorType: null)],
						NO_COLORS
				)
		)
	}

	@ParameterizedTest
	@MethodSource('shouldCreateRightPartitionsWithDryReplaceArgs')
	void shouldCreateRightPartitionsWithDryReplace(Set<PatternData> patternData, List<? extends LinePart> expectedLineParts,
			boolean disableColors) {
		def partitioner = new LinePartitioner(patternData, true, true, disableColors)
		assert partitioner.partition('this is a test') == expectedLineParts
	}

	static Stream<Arguments> shouldCreateRightPartitionsWithDryReplaceArgs() {
		Stream.of(
				Arguments.of(
						[new PatternData(searchPattern: ~/a test/, replace: true, replaceText: 'A TEST')] as Set,
						[
								new LinePart(text: 'this is ', colorType: null),
								new LinePart(text: 'A TEST', colorType: DRY_REPLACE_COLOR)
						],
						WITH_COLORS
				)
		)
	}
}
