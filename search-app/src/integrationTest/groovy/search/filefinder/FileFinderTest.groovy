package search.filefinder

import static java.nio.file.Files.copy
import static java.nio.file.Files.createDirectories
import static java.nio.file.Files.createSymbolicLink
import static search.testutil.GroovyAssertions.assertAll

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import search.conf.Conf
import search.conf.GlobPattern
import search.log.LogMock

class FileFinderTest {

	@TempDir
	protected File tempDir

	private final List<File> foundFiles = []

	@BeforeEach
	void setUp() {
		def exampleClass = new File(this.class.classLoader.getResource('example.class').toURI())
		def exampleGroovy = new File(this.class.classLoader.getResource('example.groovy').toURI())
		def greekTxt = new File(this.class.classLoader.getResource('greek.txt').toURI())

		copyFileToPathCreatingDirs exampleClass, 'a/b/c'
		copyFileToPathCreatingDirs exampleGroovy, 'a/d'
		copyFileToPathCreatingDirs greekTxt, 'a/e'
	}

	private void copyFileToPathCreatingDirs(File file, String parentDirStr) {
		def parentDir = tempDir.toPath().resolve parentDirStr

		createDirectories parentDir
		copy file.toPath(), parentDir.resolve(file.name)
	}

	@Test
	void onePattern_noMatch() {
		// When
		findFiles new Conf(paths: [new GlobPattern('nonexisting.file')])

		// Then
		assert !foundFiles
	}

	@Test
	void onePattern_withMatch_fileExcluded() {
		// When
		findFiles new Conf(
				paths: [new GlobPattern('*.groovy')],
				excludeFilePatterns: [~$//example.+/$]
		)

		// Then
		assert !foundFiles
	}

	@Test
	void onePattern_withMatch_directoryExcluded() {
		// When
		findFiles new Conf(
				paths: [new GlobPattern('*.groovy')],
				excludeFilePatterns: [~$/a/d//$]
		)

		// Then
		assert !foundFiles
	}

	@Test
	void twoPatterns_withMatch() {
		// When
		findFiles new Conf(
				paths: [new GlobPattern('*.groovy'), new GlobPattern('*.txt')]
		)

		// Then
		assertAll(
				{ assert foundFiles?.size() == 2 },
				{ assert foundFiles.any { it.path =~ $/\ba/d/example.groovy/$ } },
				{ assert foundFiles.any { it.path =~ $/\ba/e/greek.txt/$ } }
		)
	}

	@Test
	void symbolicLinksAreSkipped() {
		// Given
		createSymbolicLink tempDir.toPath().resolve('a/d/example2.groovy'), tempDir.toPath().resolve('a/d/example.groovy')

		// When
		findFiles new Conf(paths: [new GlobPattern('*.groovy')])

		// Then
		assert foundFiles?.size() == 1
	}

	@Test
	void binaryFilesAreSkipped() {
		// When
		findFiles new Conf(paths: [new GlobPattern('*.class')])

		// Then
		assert !foundFiles
	}

	@Test
	void filesAreSorted() {
		// Given
		copy tempDir.toPath().resolve('a/d/example.groovy'), tempDir.toPath().resolve('a/d/anotherExample.groovy')

		// When
		findFiles new Conf(paths: [new GlobPattern('*.groovy')])

		// Then
		assertAll(
				{ assert foundFiles?.size() == 2 },
				{ assert foundFiles[0].path =~ $/a/d/anotherExample.groovy/$ },
				{ assert foundFiles[1].path =~ $/a/d/example.groovy/$ }
		)
	}

	private void findFiles(Conf conf) {
		new FileFinder(conf, LogMock.get(), tempDir).find {
			foundFiles << it
		}
	}

}
