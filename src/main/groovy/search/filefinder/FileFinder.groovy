package search.filefinder

import static groovy.io.FileType.FILES
import static groovy.io.FileVisitResult.CONTINUE
import static groovy.io.FileVisitResult.SKIP_SUBTREE
import static java.nio.file.Files.isRegularFile
import static java.nio.file.Files.isSymbolicLink

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import search.conf.Conf
import search.log.Log

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Searches for files based on the given include and exclude patterns.
 */
@CompileStatic
class FileFinder {

	protected final Conf conf

	protected final Log log

	protected final Path baseDir

	FileFinder(Conf conf, Log log, Path baseDir = Paths.get('.')) {
		this.conf = conf
		this.log = log
		this.baseDir = baseDir
	}

	void find(@ClosureParams(value = SimpleType, options = ['java.nio.file.Path']) Closure foundFileHandler) {
		def options = [
				type: FILES,
				sort: FileFinder.&sortByFilesFirstThenByName,
				preDir: { Path file ->
					filterDir(file) ? CONTINUE : SKIP_SUBTREE
				}
		]

		baseDir.traverse(options as Map) {
			if (!filterFile(it)) {
				return SKIP_SUBTREE
			}
			foundFileHandler it
			CONTINUE
		}
	}

	private static int sortByFilesFirstThenByName(Path a, Path b) {
		def aIsFile = isRegularFile a
		def bIsFile = isRegularFile b

		aIsFile == bIsFile ? a.fileName <=> b.fileName : bIsFile <=> aIsFile
	}

	protected boolean filterDir(Path file) {
		// For simpler search patterns.
		def filePath = "$file/"

		if (isExcluded(filePath)) {
			return false
		}

		if (conf.debug > 1) {
			log.debug "*** Traversing: $filePath"
		}

		true
	}

	protected boolean filterFile(Path file) {
		if (isSymbolicLink(file)) {
			return false
		}
		if (isExcluded(file as String)) {
			return false
		}
		if (!isIncluded(file)) {
			return false
		}
		if (isBinaryFile(file)) {
			return false
		}

		if (conf.debug > 1) {
			log.debug "*** Checking: $file"
		}

		true
	}

	protected boolean isExcluded(String filePath) {
		if (conf.excludeFilePatterns.any { filePath =~ it }) {
			if (conf.debug > 1) {
				log.debug "*** SKIPPING: $filePath"
			}

			return true
		}

		false
	}

	protected boolean isIncluded(Path file) {
		def fileNameAsPath = file.fileName

		if (!conf.paths.any { it.matches(fileNameAsPath) }) {
			if (conf.debug > 1) {
				log.debug "*** SKIPPING: $file"
			}

			return false
		}

		true
	}

	private boolean isBinaryFile(Path file) {
		try {
			BinaryFileChecker.checkIfBinary file
		}
		catch (FileNotFoundException e) {
			log.error "Cannot read file: $file : $e"

			if (conf.debug) {
				log.debugException e
			}

			true
		}
	}

}
