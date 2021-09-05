# search

[![Build Status](https://github.com/rkrisztian/search/actions/workflows/gradle.yml/badge.svg)](https://github.com/rkrisztian/search/actions/workflows/gradle.yml)
[![SL Scan](https://github.com/rkrisztian/search/workflows/SL%20Scan/badge.svg)](https://github.com/rkrisztian/search/actions/workflows/shiftleft-analysis.yml)
[![codecov](https://codecov.io/gh/rkrisztian/search/branch/master/graph/badge.svg?token=02E6VF5NTQ)](https://codecov.io/gh/rkrisztian/search)

## Overview

### What it is

A simple search tool supporting multiple file patterns and keywords, and the ability to also do replaces.

### What it does

Recursively searches for files in the current directory matching the given file patterns, and having
content matching the given regular expressions. The output is colored for better readability.

The tool can also read from STDIN, which is the case if no file pattern is given.

For each search pattern you can also optionally specify a replace text.

Configuration files are supported for user-specific and project-specific file path exceptions.

### Why this tool?

I have considered [Ack](http://beyondgrep.com), but I prefer more readable outputs, sometimes I want to
search for multiple patterns, Ack cannot replace text in files, and I don't care about `vi`.

### Detailed Case Study

1. Ack was written in Perl, and requires no extra modules to install. Mine was first written in
	Perl too, but had to rewrite it in Groovy because it did use extra modules, and CPAN started
	becoming unusable. While the Groovy code can use any Java library we want. Also, Perl can be very
	complex and hard to read, and maintain.

	Groovy definitely can't beat the speed of Perl. This is an example, where `sP` was the Perl
	implementation of my tool, and `sG` was the Groovy one:

	```text
	$ time sP -a - Test
	(...)
	real	0m7.179s

	$ time sG -a - Test
	(...)
	real	0m11.902s
	```

	Although in the past I made optimizations to increase performance, I do not think this speed
	we have now is significantly bad anymore.

2. Ack limits me too much. It supports only directory names to ignore, while I support path
	patterns specified as regexes. I can't specify file name patterns in Ack while my tool supports
	glob patterns.

	While Ack ignores certain directories by default, e.g. `build`, `.git`, and my tool doesn't, I
	can still configure the same too.

3. Output is not user friendly to me.

	Example:

	```text
	$ ack --ignore-dir=build -C 3 Test src/test/groovy/search/util/GlobPatternTest.groovy

	package search.util

	import org.junit.Test

	class GlobPatternTest {

		@Test
		void globPatternWorks() {
			assert new GlobPattern('*.java').matches(Paths.get('Test.java'))
		}
	}
	```

	```text
	$ build/dist/search \*GlobPatternTest\* - Test
	./src/test/groovy/search/util/GlobPatternTest.groovy :
				 (...)

				 package search.util

			 9 : import org.junit.Test

			11 : class GlobPatternTest {

			13 : 	@Test
					void globPatternWorks() {
			15 : 		assert new GlobPattern('*.java').matches(Paths.get('Test.java'))
					}
	```

	It must be a bug that line numbers disappear when I specify an exact file path in Ack, because
	normally it looks like this:

	```text
	src/test/groovy/search/util/GlobPatternTest.groovy
	6-
	7-package search.util
	8-
	9:import org.junit.Test
	10-
	11:class GlobPatternTest {
	12-
	13:	@Test
	14-	void globPatternWorks() {
	15:		assert new GlobPattern('*.java').matches(Paths.get('Test.java'))
	16-	}
	17-}
	```

	Even with colors I find it ugly.

## Installation

### Requirements

* The tool was written primarily for use on Linux & Mac, but git bash for Windows is also supported. The implemented binary file
  detection does not work natively on Windows, as [text encoding on there is quite
  different](https://superuser.com/questions/294219/what-are-the-differences-between-linux-and-windows-txt-files-unicode-encoding).
* Java Runtime Environment version 11 or newer

### From release

Unzip/untar the release package to a directory you prefer, e.g. `~/programs/`, and rename
`~/programs/search-<VERSION>` to `~/programs/search`.

Then the executable to run is `~/programs/search/bin/search`, however, please see
chapter _Usage_ about how to make the tool easier to use.

### From source

Assuming you have [gng](https://github.com/gdubw/gng) installed, execute the following steps:

```bash
cd ~/projects
git clone $URL
cd search
gw assemble
```

Then either you can unpack the distribution package in `build/distributions` as described in the previous section, or you can
run `gw installDist` and use `build/install/search` as the extracted location.

Optionally, to ensure you got a working revision:

```bash
gw test integrationTest
```

## Usage

### Usage tips

1. Create a configuration file at `~/.search.conf`, then put generic file path exclude patterns
	there. See configuration example below.

2. *Optionally*, create a configuration file for each project, eg. under `./.local`, and
	put your project-specific exclude patterns in there. (Exclude everything that makes your
	results shown practically twice. Usually you want to exclude build directories.)

	You can also include the default configuration created in the previous step, with variable
	`includeConfig`.

3. To support automatic use of the project-specific configuration, create the following bash
	function, and put it into your private `~/.bashrc`:

	```bash
	search() {
		local args=''

		if [ -f '.local/search.conf' ]; then
			args='-c .local/search.conf'
		fi

		~/programs/search/bin/search $args "$@"
	}

	# Also add a much shorter alias because we hate typing.
	alias s=search
	```

### Command line flags

For a list of command line arguments see:

```bash
s --help
```

## Configuration

### Configuration file format

Uses Groovy syntax, see [ConfigSlurper](http://docs.groovy-lang.org/latest/html/gapi/groovy/util/ConfigSlurper.html).

### Configuration options

* `contextLines: int`
  Number of context lines to display (before and after matched lines).

* `excludePatterns: List<String>`
  List of regular expressions to exclude directory paths and file paths. You can of course use slashy strings and dollar-slashy
  strings, so you don't have to escape characters.

* `includeConfig: List<String>`
  Allows extension of the current config file with other ones. (Warning: avoid circular includes!) Supports both absolute
  paths and paths relative to the current config file. The included config will always override options of its parent config,
  regardless of where you put this option.

* `printHtml: boolean`
  Print the results in HTML and open in a browser.

* `tmpDir: String`
  Location of the temporary directory used for replacements and HTML output. The default value is taken from Java system
  property `java.io.tmpdir`.

### Example configuration

The following is an actual config I use as a user-specific config (`~/.search.conf`):

```groovy
maxContextLines = 3

excludeFilePatterns = [
	$//\.git//$,
	$//build//$,
	$//dist//$,
	$//target(.+?)//$,
	$//node_modules//$,
	$//\.gradle//$,
	$//\.idea//$,
	$//out//$,
	$//bin//$,
	/\.bak$/,
	$/\.\#(.+?)(\.\d+)+/$
]
```

In this example we automatically skip searching in text files that are generated/built
(a fine-tuning of this may desired for your project), ones that are SVN metafiles, and ones
that are temporary files created by Eclipse to show history.)

## Example usage

### Example searches

* Basic searches:

	```bash
	s \*.js - jQuery
	s \*.java - 'System\.out\.println'
	s \*.java \*.js - myCode 'myThing(s)?'
	s \*.java - MyCode '(my|their)code'
	```
* Case insensitive search:

	```bash
	s -i \* - regex processor
	```

* Hide the keyword `processor` in results:

	```bash
	s \* - regex -h processor
	```

* Search for the character sequence `-h`:

	```bash
	s \* - '\-h'
	```

* Search from the output of command `git blame`:

	```bash
	git blame someFile | search - someAuthor
	```

* Search in every file (including hidden ones):

	```bash
	s -a - myproject
	```

* Execute a command for every file found (which can be much faster than regular `findf`):
	```bash
	s -C .project | xargs git add
	```

### Example replaces

* Search for `jQuery` and replace each occurrence with `$`:

	```bash
	s \* - 'jQuery' -r '$'
	```

* Search for `jQuery` and replace each occurrence with `$`, but only in files including a script tag:

	```bash
	s \* - 'jQuery' -r '$' '<script'
	```

## How to contribute

1. Import project in IntelliJ IDEA (e.g. the Community Edition).
2. Make sure Project Code Style is used as set in `.editorconfig` (This is needed to keep formatting compatible with CodeNarc.)
3. Before committing:
	```bash
	gw clean build
	```
4. How to commit:
	Please send a PR. :)

	I will carefully review it during my free time. (This is not a paid project.)

5. Read some docs:

	* [Groovy Language Documentation](http://docs.groovy-lang.org/latest/html/documentation/)
	* [Style guide](http://www.groovy-lang.org/style-guide.html)
