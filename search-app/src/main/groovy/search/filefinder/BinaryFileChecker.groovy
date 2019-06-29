package search.filefinder

import search.log.ILog

/**
 * Perl's binary file detector ("-B <file-path>") is excellent, but we got no competent alternative
 * in Groovy. So for now we are cheating by using Perl a bit. Please note: only core Perl is used,
 * no modules!
 */
class BinaryFileChecker {

	private static final String TEXT = '0'
	private static final String BINARY = '1'

	private ILog log
	private Process process
	private InputStreamReader reader
	private OutputStreamWriter writer

	BinaryFileChecker(ILog log) {
		this.log = log
	}

	boolean checkIfBinary(File file) {
		lazyInit()

		def line = null

		try {
			writer.write file.path + '\n'
			writer.flush()
			line = reader.readLine()
		}
		catch (IOException e) {
			handleStreamError line
		}

		if (!(line in [TEXT, BINARY])) {
			handleLineError line
		}

		line == BINARY
	}

	private void lazyInit() {
		if (process) {
			return
		}

		process = new ProcessBuilder([
			'perl',
			'-e',
			'$| = 1; while (<>) { chomp(my $f = $_); print (-B $f ? 1 : 0); print "\n" }'
		]).redirectErrorStream(true).start();

		reader = new InputStreamReader(process.inputStream)
		writer = new OutputStreamWriter(process.outputStream)
	}

	private void handleStreamError(String line) {
		/*
		 * This error may happen due to streams being closed after an INT/TERM signal, in which
		 * case we should not report any errors. But if a Perl error happens we can still report on
		 * that.
		 */
		if (line == null) {
			try {
				line = reader.readLine()
			}
			catch (IOException e2) {
				// We do not know what caused it, and if we just hit CTRL+C, we must not log it.
			}
		}

		handleLineError line
	}

	private void handleLineError(String line) {
		if (line == null) {
			System.exit 2
		}

		log.fatal "Perl process error: ${line}"
	}
}
