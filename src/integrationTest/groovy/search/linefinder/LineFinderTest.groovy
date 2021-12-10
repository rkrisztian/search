package search.linefinder

import static java.nio.file.Files.copy
import static java.nio.file.Files.getPosixFilePermissions
import static java.nio.file.Files.notExists
import static java.nio.file.Files.setPosixFilePermissions
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE

import search.conf.Conf
import search.conf.PatternData
import search.log.LogMock
import search.resultsprinter.IResultsPrinter
import spock.lang.ResourceLock
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path
import java.nio.file.Paths

class LineFinderTest extends Specification {

	private static final String EXAMPLE_GROOVY_FILE_NAME = 'example.groovy'

	private final Path exampleGroovyFile = Paths.get this.class.classLoader.getResource(EXAMPLE_GROOVY_FILE_NAME).toURI()

	private String filePath

	private List<FoundLine> foundLines

	@TempDir
	private Path tempDir

	void 'prints only file name when no patterns'() {
		given:
			def lineFinder = makeLineFinderFor(new Conf())

		when:
			lineFinder.findLines exampleGroovyFile

		then:
			verifyAll {
				Paths.get(filePath).fileName as String == EXAMPLE_GROOVY_FILE_NAME
				!foundLines
			}
	}

	void 'finds lines with patterns'() {
		given:
			def lineFinder = makeLineFinderFor(new Conf(
					patternData: [
							new PatternData(searchPattern: ~/private/),
							new PatternData(searchPattern: ~/static/)
					]
			))

		when:
			lineFinder.findLines exampleGroovyFile

		then:
			verifyAll {
				Paths.get(filePath).fileName as String == EXAMPLE_GROOVY_FILE_NAME
				foundLines?.size() == 2
				foundLines?.every { it.line =~ /private static/ }
			}
	}

	void 'does not find excluded lines'() {
		given:
			def lineFinder = makeLineFinderFor(new Conf(
					patternData: [
							new PatternData(searchPattern: ~/private/),
							new PatternData(searchPattern: ~/static/)
					],
					excludeLinePatterns: [~/GREEN/, ~/class/]
			))

		when:
			lineFinder.findLines exampleGroovyFile

		then:
			verifyAll(foundLines) {
				it?.size() == 1
				it?.every { it.line =~ /private static final String RED/ }
			}
	}

	void 'does not find any lines when negative pattern matches at least once'() {
		given:
			def lineFinder = makeLineFinderFor(new Conf(
					patternData: [
							new PatternData(searchPattern: ~/static/),
							new PatternData(searchPattern: ~/class/, negativeSearch: true)
					]
			))

		when:
			lineFinder.findLines exampleGroovyFile

		then:
			verifyAll {
				!filePath
				!foundLines
			}
	}

	void 'finds lines when negative pattern never matches'() {
		given:
			def lineFinder = makeLineFinderFor(new Conf(
					patternData: [
							new PatternData(searchPattern: ~/private/),
							new PatternData(searchPattern: ~/BLUE/, negativeSearch: true)
					]
			))

		when:
			lineFinder.findLines exampleGroovyFile

		then:
			verifyAll(foundLines) {
				it?.size() == 2
				it?.every { it.line =~ /private static/ }
			}
	}

	void 'does not show lines with pattern to hide'() {
		given:
			def lineFinder = makeLineFinderFor(new Conf(
					patternData: [new PatternData(searchPattern: ~/private/, hidePattern: true)]
			))

		when:
			lineFinder.findLines exampleGroovyFile

		then:
			verifyAll {
				Paths.get(filePath).fileName as String == EXAMPLE_GROOVY_FILE_NAME
				!foundLines
			}
	}

	void 'lets results printer show replacements'() {
		given:
			def lineFinder = makeLineFinderFor(new Conf(
					patternData: [new PatternData(searchPattern: ~/private/, replace: true, replaceText: 'public')],
					doReplace: true,
					dryRun: true
			))

		when:
			lineFinder.findLines exampleGroovyFile

		then:
			verifyAll {
				Paths.get(filePath).fileName as String == EXAMPLE_GROOVY_FILE_NAME
				foundLines?.size() == 2
				foundLines?.every { it.line =~ /private static/ }
			}
	}

	@ResourceLock('search.pl.out')
	void 'does replacements in file'() {
		given:
			def exampleGroovyFileCopy = copyExampleGroovyFile tempDir
			def lineFinder = makeLineFinderFor(new Conf(
					patternData: [new PatternData(searchPattern: ~/private/, replace: true, replaceText: 'public')],
					doReplace: true,
					dryRun: false
			))

		when:
			lineFinder.findLines exampleGroovyFileCopy

		then:
			verifyAll {
				Paths.get(filePath).fileName as String == exampleGroovyFileCopy.fileName as String
				foundLines?.size() == 2
				foundLines?.every { it.line =~ /private static/ }
				exampleGroovyFileCopy.readLines().every { !(it =~ /private static/) }
			}
	}

	@ResourceLock('search.pl.out')
	void 'does not replace excluded lines'() {
		given:
			def exampleGroovyFileCopy = copyExampleGroovyFile tempDir
			def lineFinder = makeLineFinderFor(new Conf(
					patternData: [new PatternData(searchPattern: ~/private/, replace: true, replaceText: 'public')],
					doReplace: true,
					dryRun: false,
					excludeLinePatterns: [~/RED/]
			))

		when:
			lineFinder.findLines exampleGroovyFileCopy

		then:
			verifyAll {
				foundLines?.size() == 1
				foundLines?.every { it.line =~ /private static final String GREEN/ }
				exampleGroovyFileCopy.readLines().every { !(it =~ /private static final String GREEN/) }
				notExists lineFinder.replaceTmpFilePath
			}
	}

	@ResourceLock('search.pl.out')
	void 'keeps permissions when replacing'() {
		given:
			def exampleGroovyFileCopy = copyExampleGroovyFile tempDir
			setPosixFilePermissions exampleGroovyFileCopy, [OWNER_EXECUTE, OWNER_READ, OWNER_WRITE] as Set

			def lineFinder = makeLineFinderFor(new Conf(
					patternData: [new PatternData(searchPattern: ~/private/, replace: true, replaceText: 'PRIVATE')],
					doReplace: true,
					dryRun: false
			))

		when:
			lineFinder.findLines exampleGroovyFileCopy

		then:
			verifyAll {
				foundLines
				OWNER_EXECUTE in getPosixFilePermissions(exampleGroovyFileCopy)
			}
	}

	private LineFinder makeLineFinderFor(Conf conf) {
		def mockResultsPrinter = [
				printFoundLines: { filePath, foundLines ->
					this.filePath = filePath
					this.foundLines = foundLines
				}
		] as IResultsPrinter

		def linesCollector = new LinesCollector(conf.maxMatchedLinesPerFile, conf.maxContextLines,
				Conf.MAX_DISPLAYED_LINE_LENGTH)
		new LineFinder(conf, linesCollector, mockResultsPrinter, LogMock.get())
	}

	private Path copyExampleGroovyFile(Path tempDir) {
		def exampleGroovyFileCopy = tempDir.resolve exampleGroovyFile.fileName
		copy exampleGroovyFile, exampleGroovyFileCopy, REPLACE_EXISTING

		exampleGroovyFileCopy
	}

}
