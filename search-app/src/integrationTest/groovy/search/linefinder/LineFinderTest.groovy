package search.linefinder

import static java.nio.file.Files.copy
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING
import static search.testutil.GroovyAssertions.assertAll

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import search.conf.Conf
import search.conf.PatternData
import search.log.LogMock
import search.resultsprinter.IResultsPrinter

import java.nio.file.Path

class LineFinderTest {

	private static final String EXAMPLE_GROOVY_FILE_NAME = 'example.groovy'

	private final File exampleGroovyFile = new File(this.class.classLoader.getResource(EXAMPLE_GROOVY_FILE_NAME).toURI())

	private String filePath

	private List<FoundLine> foundLines

	@Test
	void shouldPrintOnlyFileNameWhenNoPatterns() {
		// Given
		def lineFinder = makeLineFinderFor(new Conf())

		// When
		lineFinder.findLines exampleGroovyFile

		// Then
		assertAll(
				{ assert new File(filePath).name == EXAMPLE_GROOVY_FILE_NAME },
				{ assert !foundLines }
		)
	}

	@Test
	void shouldFindLinesWithPatterns() {
		// Given
		def lineFinder = makeLineFinderFor(new Conf(
				patternData: [
						new PatternData(searchPattern: ~/private/),
						new PatternData(searchPattern: ~/static/)
				]
		))

		// When
		lineFinder.findLines exampleGroovyFile

		// Then
		assertAll(
				{ assert new File(filePath).name == EXAMPLE_GROOVY_FILE_NAME },
				{ assert foundLines?.size() == 2 },
				{ assert foundLines?.every { it.line =~ /private static/ } }
		)
	}

	@Test
	void mustFindAllPatternsEvenWithExcludePatternsAdded() {
		// Given
		def lineFinder = makeLineFinderFor(new Conf(
				patternData: [
						new PatternData(searchPattern: ~/class/),
						new PatternData(searchPattern: ~/private/)
				],
				excludeLinePatterns: [~/static/]
		))

		// When
		lineFinder.findLines exampleGroovyFile

		println filePath

		// Then
		assertAll(
				{ assert !filePath },
				{ assert !foundLines }
		)
	}

	@Test
	void shouldNotFindLinesWithNegativePattern() {
		// Given
		def lineFinder = makeLineFinderFor(new Conf(
				patternData: [
						new PatternData(searchPattern: ~/private/),
						new PatternData(searchPattern: ~/static/, negativeSearch: true)
				]
		))

		// When
		lineFinder.findLines exampleGroovyFile

		println filePath

		// Then
		assertAll(
				{ assert !filePath },
				{ assert !foundLines }
		)
	}

	@Test
	void shouldNotShowLinesWithPatternToHide() {
		// Given
		def lineFinder = makeLineFinderFor(new Conf(
				patternData: [new PatternData(searchPattern: ~/private/, hidePattern: true)]
		))

		// When
		lineFinder.findLines exampleGroovyFile

		println filePath

		// Then
		assertAll(
				{ assert new File(filePath).name == EXAMPLE_GROOVY_FILE_NAME },
				{ assert !foundLines }
		)
	}

	@Test
	void shouldLetResultsPrinterShowReplacements() {
		// Given
		def lineFinder = makeLineFinderFor(new Conf(
				patternData: [new PatternData(searchPattern: ~/private/, replace: true, replaceText: 'public')],
				doReplace: true,
				dryRun: true
		))

		// When
		lineFinder.findLines exampleGroovyFile

		// Then
		assertAll(
				{ assert new File(filePath).name == EXAMPLE_GROOVY_FILE_NAME },
				{ assert foundLines?.size() == 2 },
				{ assert foundLines?.every { it.line =~ /private static/ } }
		)
	}

	@Test
	void shouldDoReplacementsInFile(@TempDir Path tempDir) {
		// Given
		def exampleGroovyFileCopy = copyExampleGroovyFile tempDir
		def lineFinder = makeLineFinderFor(new Conf(
				patternData: [new PatternData(searchPattern: ~/private/, replace: true, replaceText: 'public')],
				doReplace: true,
				dryRun: false
		))

		// When
		lineFinder.findLines exampleGroovyFileCopy.toFile()

		// Then
		assertAll(
				{ assert new File(filePath).name == exampleGroovyFileCopy.fileName as String },
				{ assert foundLines?.size() == 2 },
				{ assert foundLines?.every { it.line =~ /private static/ } },
				{ assert exampleGroovyFileCopy.readLines().every { !(it =~ /private static/) } }
		)
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
		def exampleGroovyFileCopy = tempDir.resolve(exampleGroovyFile.name)
		copy exampleGroovyFile.toPath(), exampleGroovyFileCopy, REPLACE_EXISTING

		exampleGroovyFileCopy
	}

}
