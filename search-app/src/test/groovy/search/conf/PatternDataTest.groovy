package search.conf

import spock.lang.Specification

class PatternDataTest extends Specification {

	@SuppressWarnings(['ComparisonWithSelf', 'GrEqualsBetweenInconvertibleTypes'])
	void 'basic equals behavior'() {
		expect:
			verifyAll(new PatternData(searchPattern: ~/abc/)) {
				it == it
				it != null
				null != it
				it != new Conf()
				it == new PatternData(searchPattern: ~/abc/)
			}
	}

	void 'equals check depends only on the search pattern'() {
		expect:
			verifyAll {
				new PatternData(searchPattern: ~/abc/) == new PatternData(searchPattern: ~/abc/)
				new PatternData(searchPattern: ~/abc/) == new PatternData(searchPattern: ~/abc/, hidePattern: true)
				new PatternData(replaceText: true, replace: 'x') == new PatternData()
				new PatternData() == new PatternData(replace: true, replaceText: 'x')

				new PatternData(searchPattern: ~/abc/) != new PatternData(searchPattern: ~/abcd/)
				new PatternData(searchPattern: ~/abc/) != new PatternData()
				new PatternData(negativeSearch: true) != new PatternData(searchPattern: ~/abc/)
			}
	}

}
