# search [![Build Status](https://travis-ci.org/rkrisztian/search.svg?branch=master)](https://travis-ci.org/rkrisztian/search)

## Overview

### What it is

A simple search tool supporting multiple file patterns and keywords, and the ability to also do replaces.

Written primarily for use on Linux & Mac. The binary file detection works there only, as [text encoding on Windows is quite
different](https://superuser.com/questions/294219/what-are-the-differences-between-linux-and-windows-txt-files-unicode-encoding).

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
	becoming unusable. While the Groovy code can use any Java library we want. Also Perl can be very
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
	$ ack --ignore-dir=build -C 3 Test \
			search-app/src/test/groovy/search/util/GlobPatternTest.groovy

	package search.util

	import org.junit.Test

	class GlobPatternTest {

		@Test
		void globPatternWorks() {
			assert new GlobPattern('*.java').matches(new File('Test.java').toPath())
		}
	}
	```

	```text
	$ search-app/build/dist/search \*GlobPatternTest\* - Test
	./search-app/src/test/groovy/search/util/GlobPatternTest.groovy :
				 (...)

				 package search.util

			 9 : import org.junit.Test

			11 : class GlobPatternTest {

			13 : 	@Test
					void globPatternWorks() {
			15 : 		assert new GlobPattern('*.java').matches(new File('Test.java').toPath())
					}
	```

	It must be a bug that line numbers disappear when I specify an exact file path in Ack, because
	normally it looks like this:

	```text
	search-app/src/test/groovy/search/util/GlobPatternTest.groovy
	6-
	7-package search.util
	8-
	9:import org.junit.Test
	10-
	11:class GlobPatternTest {
	12-
	13:	@Test
	14-	void globPatternWorks() {
	15:		assert new GlobPattern('*.java').matches(new File('Test.java').toPath())
	16-	}
	17-}
	```

	Even with colors I find it ugly.

## Installation

Execute the following steps:

```text
$ cd ~/projects
$ git clone $URL
$ cd search
$ gw assemble
```

Optionally, to ensure you got a working revision:

```text
$ gw test integrationTest
```

Then to make it easier to use, see next section.

## Usage

### Usage tips

1. Create a configuration file at `~/.search.conf`, then put generic file path exclude patterns
	there. See configuration example below.

2. Optionally, create a configuration file for each project, eg. under `./!local`, and
	put your project-specific exclude patterns in there. (Exclude everything that makes your
	results shown practically twice. Usually you want to exclude build directories.)

	You can also include the default configuration created in the previous step, with variable
	`includeConfig`.

3. To support automatic use of the project-specific configuration, create the following bash
	function, and put it into your private `~/.bashrc`:

	```text
	search() {
		local args=''

		if [ -f './!local/.search.conf' ]; then
			args='-c ./!local/.search.conf'
		fi

		~/projects/search/search-app/build/distributions/search $args "$@"
	}

	# Also add a much shorter alias because we hate typing.
	alias s=search
	```

### Command line flags

For a list of command line arguments see:

```text
$ s --help
```

## Configuration

### Configuration file format

Uses Groovy syntax, see [ConfigSlurper](http://docs.groovy-lang.org/latest/html/gapi/groovy/util/ConfigSlurper.html).

### Configuration options

* `excludePatterns: List<String>`

	List of regular expressions to exclude directory and file paths. You can of course use
		slashy strings and dollar-slashy strings so you don't have to escape characters.

* `includeConfig: List<String>`

	Allows extension of the current config file with other ones. (Warning: avoid circular
	includes!)

* `contextLines: int`

	Number of context lines to display (before and after matched lines).

* `printHtml: boolean`

	Print the results in HTML and open in a browser.

### Example configuration

The following is an actual config I use as a user-specific config (`~/.search.conf`):

```groovy
maxContextLines = 3

excludeFilePatterns = [
	$/\.\#(.+?)(\.\d+)+/$,
	$//.git//$,
	$//build//$,
	$//out//$,
	$//bin//$,
	$//dist//$,
	$//target(.+?)//$,
	/\.bak$/,
	$//node_modules//$,
	$//\.idea//$,
	$//\.gradle//$
]
```

In this example we automatically skip searching in text files that are generated/built
(a fine-tuning of this may desired for your project), ones that are SVN metafiles, and ones
that are temporary files created by Eclipse to show history.)

## Example usage

### Example searches

* Basic searches:

	```text
	$ s \*.js - jQuery
	$ s \*.java - 'System\.out\.println'
	$ s \*.java \*.js - myCode 'myThing(s)?'
	$ s \*.java - MyCode '(my|their)code'
	```
* Case insensitive search:

	```text
	$ s -i \* - regex processor
	```

* Hide the keyword `processor` in results:

	```text
	$ s \* - regex -h processor
	```

* Search for the character sequence `-h`:

	```text
	$ s \* - '\-h'
	```

* Search for HTML/JS/CSS files containing the text `elementId` (might not cover all files in your project):

	```text
	$ s -w - 'elementId'
	```

* Search from the output of command `git blame`:

	```text
	$ git blame someFile | search - someAuthor
	```

* Search in every files (including hidden ones):

	```text
	$ s -a - myproject
	```

* Execute a command for every file found (which can be much faster than regular `findf`):
	```text
	$ s -C .project | xargs git add
	```

### Example replaces

* Search for `jQuery` and replace each occurrence with `$`:

	```text
	$ s \* - 'jQuery' -r '$'
	``` 

* Search for `jQuery` and replace each occurrence with `$`, but only in files including a script tag:

	```text
	$ s \* - 'jQuery' -r '$' '<script'
	``` 

## How to contribute

1. Import project in IntelliJ IDEA.
2. Before committing:
	```text
	$ gw clean assemble test integrationTest
	```
3. How to commit:
	Please send a PR. :)

	I will carefully review it during my free time. (This is not a paid project.)

4. Read some docs:

	* [Groovy Language Documentation](http://docs.groovy-lang.org/latest/html/documentation/)
	* [Style guide](www.groovy-lang.org/style-guide.html) (not followed entirely yet)

## To do

* Integration tests.
* More test coverage. `LineFinder.groovy` has no tests at all.
* Unit test coverage report (no clue if it works with Groovy code).
* Consider a linter tool.
