package search.conf

import groovy.transform.CompileStatic

import java.util.regex.Pattern

@CompileStatic
class Conf implements Dumpable {

	public static final int DEFAULT_MAX_CONTEXT_LINES = 0

	/** Maximum length of a displayed line (type -1 for no limit) */
	public static final int maxDisplayedLineLength = 500

	public int debug

	public List<GlobPattern> paths = []

	public String configFile = "${System.getProperty('user.home')}/.search.conf"

	public boolean disableColors

	/** Maximum number of matches lines printed per file (type <code>-1</code> for no limit) */
	public int maxMatchedLinesPerFile = -1

	/** Maximum number of context lines displayed around matched lines. */
	public Integer maxContextLines

	public List<Pattern> excludeFilePatterns = []

	public List<Pattern> excludeLinePatterns = []

	public Set<PatternData> patternData = []

	public boolean doReplace

	public boolean dryRun

	public boolean printHtml

	void setDefaults() {
		if (maxContextLines == null) {
			maxContextLines = DEFAULT_MAX_CONTEXT_LINES
		}
	}
}
