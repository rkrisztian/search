package search.filefinder

import static groovy.io.FileType.FILES
import static groovy.io.FileVisitResult.CONTINUE
import static groovy.io.FileVisitResult.SKIP_SUBTREE
import static java.nio.file.Files.isSymbolicLink

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import search.conf.Conf
import search.log.ILog

/**
 * Searches for files based on the given include and exclude patterns.
 */
@CompileStatic
class FileFinder {

	protected final Conf conf

	protected final ILog log

	FileFinder(Conf conf, ILog log) {
		this.conf = conf
		this.log = log
	}

	void find(@ClosureParams(value = SimpleType, options = ['File']) Closure foundFileHandler) {
		def sortByFilesFirstThenByName = { File a, File b ->
			a.file == b.file ? a.name <=> b.name : b.file <=> a.file
		}
		def options = [
				type  : FILES,
				sort  : sortByFilesFirstThenByName,
				preDir: { File file ->
					filterDir(file) ? CONTINUE : SKIP_SUBTREE
				}
		]

		new File('.').traverse(options as Map) {
			if (!filterFile(it)) {
				return SKIP_SUBTREE
			}
			foundFileHandler it
			CONTINUE
		}
	}

	protected boolean filterDir(File file) {
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

	protected boolean filterFile(File file) {
		if (isSymbolicLink(file.toPath())) {
			return false
		}
		if (isExcluded(file.path)) {
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

	protected boolean isIncluded(File file) {
		def fileNameAsPath = new File(file.name).toPath()

		if (!conf.paths.any { it.matches(fileNameAsPath) }) {
			if (conf.debug > 1) {
				log.debug "*** SKIPPING: $file"
			}

			return false
		}

		true
	}

	private boolean isBinaryFile(File file) {
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
