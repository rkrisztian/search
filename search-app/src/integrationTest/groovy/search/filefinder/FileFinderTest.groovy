package search.filefinder

import static java.nio.file.Files.copy
import static java.nio.file.Files.createDirectories
import static java.nio.file.Files.createSymbolicLink

import search.conf.Conf
import search.conf.GlobPattern
import search.log.LogMock
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path
import java.nio.file.Paths

class FileFinderTest extends Specification {

	@TempDir
	private Path tempDir

	private final List<Path> foundFiles = []

	void setup() {
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

	void 'one pattern, no match'() {
		when:
			findFiles new Conf(paths: [new GlobPattern('nonexisting.file')])

		then:
			!foundFiles
	}

	void 'one pattern, with match, file excluded'() {
		when:
			findFiles new Conf(
					paths: [new GlobPattern('*.groovy')],
					excludeFilePatterns: [~$//example.+/$]
			)

		then:
			!foundFiles
	}

	void 'one pattern, with match, directory excluded'() {
		when:
			findFiles new Conf(
					paths: [new GlobPattern('*.groovy')],
					excludeFilePatterns: [~$/a/d//$]
			)

		then:
			!foundFiles
	}

	void 'two patterns, with match'() {
		when:
			findFiles new Conf(
					paths: [new GlobPattern('*.groovy'), new GlobPattern('*.txt')]
			)

		then:
			foundFiles?.size() == 2
			foundFiles.any { it as String =~ $/\ba/d/example.groovy/$ }
			foundFiles.any { it as String =~ $/\ba/e/greek.txt/$ }
	}

	void 'symbolic links are skipped'() {
		given:
			createSymbolicLink tempDir.resolve('a/d/example2.groovy'), tempDir.resolve('a/d/example.groovy')

		when:
			findFiles new Conf(paths: [new GlobPattern('*.groovy')])

		then:
			foundFiles?.size() == 1
	}

	void 'binary files are skipped'() {
		when:
			findFiles new Conf(paths: [new GlobPattern('*.class')])

		then:
			!foundFiles
	}

	void 'files are sorted'() {
		given:
			copy tempDir.resolve('a/d/example.groovy'), tempDir.resolve('a/d/anotherExample.groovy')

		when:
			findFiles new Conf(paths: [new GlobPattern('*.groovy')])

		then:
			foundFiles?.size() == 2
			foundFiles[0] as String =~ $/a/d/anotherExample.groovy/$
			foundFiles[1] as String =~ $/a/d/example.groovy/$
	}

	private void findFiles(Conf conf) {
		new FileFinder(conf, LogMock.get(), tempDir).find {
			foundFiles << it
		}
	}

}
