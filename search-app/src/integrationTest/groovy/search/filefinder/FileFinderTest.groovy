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

import java.nio.file.Path
import java.nio.file.Paths

class FileFinderTest {

	@TempDir
	protected Path tempDir

	private final List<Path> foundFiles = []

	@BeforeEach
	void setUp() {
		def exampleClass = Paths.get this.class.classLoader.getResource('example.class').toURI()
		def exampleGroovy = Paths.get this.class.classLoader.getResource('example.groovy').toURI()
		def greekTxt = Paths.get this.class.classLoader.getResource('greek.txt').toURI()

		copyFileToPathCreatingDirs exampleClass, 'a/b/c'
		copyFileToPathCreatingDirs exampleGroovy, 'a/d'
		copyFileToPathCreatingDirs greekTxt, 'a/e'
	}

	private void copyFileToPathCreatingDirs(Path file, String parentDirStr) {
		def parentDir = tempDir.resolve parentDirStr

		createDirectories parentDir
		copy file, parentDir.resolve(file.fileName)
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
				{ assert foundFiles.any { it as String =~ $/\ba/d/example.groovy/$ } },
				{ assert foundFiles.any { it as String =~ $/\ba/e/greek.txt/$ } }
		)
	}

	@Test
	void symbolicLinksAreSkipped() {
		// Given
		createSymbolicLink tempDir.resolve('a/d/example2.groovy'), tempDir.resolve('a/d/example.groovy')

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
		copy tempDir.resolve('a/d/example.groovy'), tempDir.resolve('a/d/anotherExample.groovy')

		// When
		findFiles new Conf(paths: [new GlobPattern('*.groovy')])

		// Then
		assertAll(
				{ assert foundFiles?.size() == 2 },
				{ assert foundFiles[0] as String =~ $/a/d/anotherExample.groovy/$ },
				{ assert foundFiles[1] as String =~ $/a/d/example.groovy/$ }
		)
	}

	private void findFiles(Conf conf) {
		new FileFinder(conf, LogMock.get(), tempDir).find {
			foundFiles << it
		}
	}

}
