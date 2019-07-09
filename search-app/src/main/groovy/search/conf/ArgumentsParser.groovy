package search.conf

import static java.util.regex.Pattern.quote

import groovy.transform.CompileStatic
import search.log.ILog

@CompileStatic
class ArgumentsParser {

	private static final USAGE = '''
	Usage:
	
		\$ search [<options...>] <file-patterns...> - \\
					[ [<text-pattern-options>] <text-patterns>
					  [<after-text-pattern-options>] ...]
	
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
			-w            Search in web files (HTML, JS, and CSS files). (This is purely a convenience option.)
			                  If you use it, <file-patterns...> are optional.
			-D            Dry run for the replace functionality (see "-r"). Useful for testing backreferences.
			-H            Print results in HTML format and open in a browser. (Colors not fully supported yet.)
			--help        Show this help.
	
		Text pattern options:
	
			-e            Search in EL expressions (only suitable for JSF code, in which EL expressions are not
			                  multi-line.) (This is purely a convenience option.)
			-h            Hide next text pattern from results.
			-s            Skip lines matching the given pattern. Use when easier than having to do negative lookaheads.
			-n            Negative search (counts as a match if no lines contain this pattern).
	
		After-text-pattern options:
	
			-r <text>     Perform a replace on the previous pattern. Supports backreferences.
'''

	private final Conf conf
	private final ILog log

	ArgumentsParser(Conf conf, ILog log) {
		this.conf = conf
		this.log = log
	}

	boolean parseArgs(String[] args) {
		// TODO 12-Nov-2015/rkrisztian: Learn CliBuilder, and try to use it here.
		def argsIterator = args.iterator()

		def showHelp = true
		def beforeHypen = true
		def hideNextTextPattern = false
		def searchNextPatternInElExps = false
		def regexOptions = ''
		def nextPatternIsNegativeSearch = false

		argLoop: while (argsIterator.hasNext()) {
			def arg = argsIterator.next()

			showHelp = false

			if (beforeHypen) {
				// Process option arguments.
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
					case '-w':
						conf.paths << new GlobPattern('*.html')
						conf.paths << new GlobPattern('*.xhtml')
						conf.paths << new GlobPattern('*.js')
						conf.paths << new GlobPattern('*.css')
						break
					case '--help':
						showHelp = true
						break argLoop
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
			else {
				// Process the rest of the arguments.
				switch (arg) {
					case '-e':
						searchNextPatternInElExps = true
						break
					case '-h':
						hideNextTextPattern = true
						break
					case '-r':
						def replaceText = argsIterator.next()

						if (!conf.paths) {
							log.fatal 'Cannot replace in STDIN.'
						}
						if (!conf.patternData) {
							log.warn "No search pattern specified for replace pattern, ignoring argument ${arg}."
							continue argLoop
						}

						def lastPatternData = conf.patternData.last() as PatternData

						lastPatternData.replace = true
						lastPatternData.replaceText = replaceText
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
						def colorReplacePatternStr = searchPatternStr

						if (searchNextPatternInElExps) {
							searchPatternStr = /[\$#]\{.*?/ + quote(searchPatternStr) + /.*?\}/
						}

						conf.patternData << new PatternData(
								searchPattern: ~(regexOptions + searchPatternStr),
								colorReplacePattern: ~(regexOptions + colorReplacePatternStr),
								hidePattern: hideNextTextPattern,
								negativeSearch: nextPatternIsNegativeSearch)

						hideNextTextPattern = false
						searchNextPatternInElExps = false
						nextPatternIsNegativeSearch = false
				}
			}
		}

		if (showHelp) {
			log.info USAGE
			return false
		}

		true
	}
}
