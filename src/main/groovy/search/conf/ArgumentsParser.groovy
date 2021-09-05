package search.conf

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import search.SearchError
import search.annotations.VisibleForTesting
import search.log.ILog

/**
 * Not using a 3rd-party library to parse arguments because of the unconventional syntax.
 */
@CompileStatic
class ArgumentsParser {

	private static final String USAGE = '''
	Usage:

		\$ search [<options...>] <file-patterns...> - \\
					[ [<text-pattern-options>] <text-patterns> [<after-text-pattern-options>] ...]

		Patterns:

			<file-patterns> are the usual shell glob patterns. If no files are specified, the standard input channel
			    will be read.
			<text-patterns> are AND-connected, and are regular expressions, where you do NOT need to escape slashes (/).

		Options:

			-a            Match all files including hidden files (same as {,.}\\* in bash).
			-c <file>     Specify configuration file (default: ~/.search_pl.conf).
			-C            Disable colored output.
			-d            Debug mode. (Default: no.) (Specify twice for more verbosity.)
			-i            Ignore case. (Default: no.)
			-l <num>      Maximum matched lines displayed, -1 means no limit (default). If there are more found lines
			                  than allowed to be shown, an additional "(...)" line will indicate that.
			-s <pattern>  Skip files matching the given pattern. (Same as option "ExcludeFilePatterns".)
			-t <num>      Number of context lines to display.
			-D            Dry run for the replace functionality (see "-r"). Useful for testing backreferences.
			-H            Print results in HTML format and open in a browser. (Colors not fully supported yet.)
			--help        Show this help.

		Text pattern options:

			-h            Hide next text pattern from results.
			-s            Skip lines matching the given pattern. Use when easier than having to do negative lookaheads.
			-n            Negative search (counts as a match if no lines contain this pattern).

		After-text-pattern options:

			-r <text>     Perform a replace on the previous pattern. Supports backreferences.
	'''.stripIndent(true)

	private final Conf conf
	private final ILog log

	@VisibleForTesting
	@PackageScope
	boolean showHelp = false

	private boolean beforeHypen = true
	private boolean hideNextTextPattern = false
	private String regexOptions = ''
	private boolean nextPatternIsNegativeSearch = false
	private Iterator<String> argsIterator

	ArgumentsParser(Conf conf, ILog log) {
		this.conf = conf
		this.log = log
	}

	boolean parseArgs(String[] args) {
		if (args) {
			argsIterator = args.iterator()

			while (argsIterator.hasNext() && !showHelp) {
				def arg = argsIterator.next()

				if (beforeHypen) {
					parseOptionOrFilePattern arg
				}
				else {
					parseTextOptionOrTextPattern arg
				}
			}
		}
		else {
			showHelp = true
		}

		if (showHelp) {
			log.info USAGE
			return false
		}

		true
	}

	private void parseOptionOrFilePattern(String arg) {
		switch (arg) {
			case '-a':
				conf.paths << new GlobPattern('*')
				conf.paths << new GlobPattern('.*')
				break
			case '-c':
				conf.configFile = argsIterator.next()
				break
			case '-C':
				conf.disableColors = true
				break
			case '-d':
				conf.debug++
				break
			case '-D':
				conf.dryRun = true
				break
			case '-H':
				conf.printHtml = true
				break
			case '-i':
				regexOptions = '(?i)'
				break
			case '-l':
				conf.maxMatchedLinesPerFile = argsIterator.next() as int
				break
			case '-s':
				conf.excludeFilePatterns << ~argsIterator.next()
				break
			case '-t':
				conf.maxContextLines = argsIterator.next() as int
				break
			case '--help':
				showHelp = true
				break
			case '-':
				beforeHypen = false
				break
			default:
				if (arg =~ /^-/) {
					log.warn "Argument ${arg} looks like an option, it is interpreted as a file pattern."
				}

				conf.paths << new GlobPattern(arg)
		}
	}

	private void parseTextOptionOrTextPattern(String arg) {
		switch (arg) {
			case '-h':
				hideNextTextPattern = true
				break
			case '-r':
				def replaceText = argsIterator.next()

				if (!conf.paths) {
					throw new SearchError('Cannot replace in STDIN.')
				}
				if (!conf.patternData) {
					log.warn "No search pattern specified for replace pattern, ignoring argument ${arg}."
					break
				}

				conf.patternData.last().with {
					replace = true
					it.replaceText = replaceText
				}

				conf.doReplace = true
				break
			case '-s':
				conf.excludeLinePatterns << ~argsIterator.next()
				break
			case '-n':
				nextPatternIsNegativeSearch = true
				break
			default:
				def searchPatternStr = arg

				conf.patternData << new PatternData(
						searchPattern: ~(regexOptions + searchPatternStr),
						hidePattern: hideNextTextPattern,
						negativeSearch: nextPatternIsNegativeSearch)

				hideNextTextPattern = false
				nextPatternIsNegativeSearch = false
		}
	}

}
