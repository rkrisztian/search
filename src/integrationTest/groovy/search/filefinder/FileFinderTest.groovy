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

	private final List<String> foundFiles = []

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
			verifyAll(foundFiles) {
				it?.size() == 2
				it?.any { it =~ $/\ba/d/example.groovy/$ }
				it?.any { it =~ $/\ba/e/greek.txt/$ }
			}
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
			verifyAll(foundFiles) {
				it?.size() == 2
				it[0] =~ $/a/d/anotherExample.groovy/$
				it[1] =~ $/a/d/example.groovy/$
			}
	}

	private void findFiles(Conf conf) {
		new FileFinder(conf, LogMock.get(), tempDir).find {
			foundFiles << (it as String)
		}
	}

}
