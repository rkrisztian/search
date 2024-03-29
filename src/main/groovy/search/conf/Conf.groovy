package search.conf

import groovy.transform.CompileStatic
import groovy.transform.ToString

import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

/**
 * Stores the configuration data.
 */
@CompileStatic
@ToString(includeFields = true, includePackage = false, includeNames = true)
class Conf {

	public static final int DEFAULT_MAX_CONTEXT_LINES = 0

	/** Maximum length of a displayed line (type -1 for no limit) */
	public static final int MAX_DISPLAYED_LINE_LENGTH = 500

	public static final Path DEFAULT_TMP_DIR = Paths.get(System.getProperty('java.io.tmpdir') ?: '/tmp')

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

	public Path tmpDir = DEFAULT_TMP_DIR

	void setDefaults() {
		maxContextLines ?= DEFAULT_MAX_CONTEXT_LINES
	}

}
