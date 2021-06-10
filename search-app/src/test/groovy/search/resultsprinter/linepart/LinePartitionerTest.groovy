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

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import search.conf.PatternData

import java.util.stream.Stream

class LinePartitionerTest {

	@Test
	void shouldPartitionEmptyLine() {
		def partitioner = new LinePartitioner([new PatternData(searchPattern: ~/dummyPattern/)] as Set,
				NO_REPLACE, NO_DRY_RUN, WITH_COLORS)
		assert partitioner.partition('') == []
	}

	@ParameterizedTest(name = '[{index}] {0}; colors: {1}')
	@MethodSource('shouldCreateRightSearchPartitionsArgs')
	void shouldCreateRightSearchPartitions(Set<PatternData> patternData, boolean disableColors,
			List<? extends LinePart> expectedLineParts) {
		def partitioner = new LinePartitioner(patternData, NO_REPLACE, NO_DRY_RUN, disableColors)
		assert partitioner.partition('this is a test') == expectedLineParts
	}

	static Stream<Arguments> shouldCreateRightSearchPartitionsArgs() {
		Stream.of(
				Arguments.of(
						[new PatternData(searchPattern: ~/this/, hidePattern: true)] as Set,
						WITH_COLORS,
						[new LinePart(text: 'this is a test')]
				),

				Arguments.of(
						[new PatternData(searchPattern: ~/this will not match/)] as Set,
						WITH_COLORS,
						[new LinePart(text: 'this is a test')]
				),

				Arguments.of(
						[new PatternData(searchPattern: ~/\s+is\s+a\s+/)] as Set,
						WITH_COLORS,
						[
								new LinePart(text: 'this'),
								new LinePart(text: ' is a ', colorType: MATCH_COLOR),
								new LinePart(text: 'test')
						]
				),

				Arguments.of(
						[new PatternData(searchPattern: ~/\s+is\s+a\s+/)] as Set,
						NO_COLORS,
						[new LinePart(text: 'this is a test')]
				),

				Arguments.of(
						[new PatternData(searchPattern: ~/t/)] as Set,
						WITH_COLORS,
						[
								new LinePart(text: 't', colorType: MATCH_COLOR),
								new LinePart(text: 'his is a '),
								new LinePart(text: 't', colorType: MATCH_COLOR),
								new LinePart(text: 'es'),
								new LinePart(text: 't', colorType: MATCH_COLOR)
						]
				),

				Arguments.of(
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
				)
		)
	}

	@ParameterizedTest(name = '[{index}] {0}; colors: {1}')
	@MethodSource('shouldCreateRightPartitionsWithReplaceArgs')
	void shouldCreateRightPartitionsWithReplace(Set<PatternData> patternData, boolean disableColors,
			List<? extends LinePart> expectedLineParts) {
		def partitioner = new LinePartitioner(patternData, WITH_REPLACE, NO_DRY_RUN, disableColors)
		assert partitioner.partition('this is a test') == expectedLineParts
	}

	static Stream<Arguments> shouldCreateRightPartitionsWithReplaceArgs() {
		Stream.of(
				Arguments.of(
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
				),

				Arguments.of(
						[
								new PatternData(searchPattern: ~/is a test/),
								new PatternData(searchPattern: ~/a test/, replace: true, replaceText: 'A TEST')
						] as Set,
						NO_COLORS,
						[new LinePart(text: 'this is A TEST')]
				),

				Arguments.of(
						[new PatternData(searchPattern: ~/is/, replace: true, replaceText: 'IISS')] as Set,
						WITH_COLORS,
						[
								new LinePart(text: 'th'),
								new LinePart(text: 'IISS', colorType: REPLACE_COLOR),
								new LinePart(text: ' '),
								new LinePart(text: 'IISS', colorType: REPLACE_COLOR),
								new LinePart(text: ' a test')
						]
				)
		)
	}

	@ParameterizedTest(name = '[{index}] {0}; colors: {1}')
	@MethodSource('shouldCreateRightPartitionsWithDryReplaceArgs')
	void shouldCreateRightPartitionsWithDryReplace(Set<PatternData> patternData, boolean disableColors,
			List<? extends LinePart> expectedLineParts) {
		def partitioner = new LinePartitioner(patternData, WITH_REPLACE, WITH_DRY_RUN, disableColors)
		assert partitioner.partition('this is a test') == expectedLineParts
	}

	@SuppressWarnings('unused')
	static Stream<Arguments> shouldCreateRightPartitionsWithDryReplaceArgs() {
		Stream.of(
				Arguments.of(
						[new PatternData(searchPattern: ~/a test/, replace: true, replaceText: 'A TEST')] as Set,
						WITH_COLORS,
						[
								new LinePart(text: 'this is '),
								new LinePart(text: 'A TEST', colorType: DRY_REPLACE_COLOR)
						]
				),

				Arguments.of(
						[new PatternData(searchPattern: ~/a test/, replace: true, replaceText: 'A TEST')] as Set,
						NO_COLORS,
						[new LinePart(text: 'this is A TEST')]
				)
		)
	}

}
