package search.filefinder
/**
 * Perl's binary file detector ("-B <file-path>") is excellent, but I found no similar library in Java or Groovy.
 *
 * The following code is a derivative of Ficando Itucan's "File is Text or Binary – Java Decision Engine":
 *     * https://www.build-business-websites.co.uk/java-is-file-text-or-binary/
 *     * https://www.build-business-websites.co.uk/file-is-text-or-binary-java-decision-engine/
 *
 * Alternatively, if needed we can look into this as well:
 *     * https://binaryornot.readthedocs.io/en/latest/readme.html
 */
class BinaryFileChecker {

	private static final int TAB_CHARACTER = 0x09
	private static final int LINE_FEED_CHARACTER = 0x0A
	private static final int FORM_FEED_CHARACTER = 0x0C
	private static final int CARRIAGE_RETURN_CHARACTER = 0x0D

	private final static int ASCII_TEXT_SYMBOLS_LOWER_BOUND = 0x20
	private final static int ASCII_TEXT_SYMBOLS_UPPER_BOUND = 0x7E
	private final static int LATIN_CHARSET_LOWER_BOUND = 0xA0
	private final static int LATIN_CHARSET_UPPER_BOUND = 0xEE
	private final static int LATIN_IN_UTF_8_LOWER_BOUND = 0x2E2E
	private static final int LATIN_IN_UTF_8_UPPER_BOUND = 0xC3BF

	boolean checkIfBinary(File file) {
		checkEachChunk(file, 512, 1) { byte[] buffer, int count ->
			checkEachByte(buffer, count) { int unsignedByte, int utf8value ->
				!isCharacterText(unsignedByte, utf8value)
			}
		}
	}

	private boolean checkEachChunk(File file, int bufferSize, int times, Closure checkChunk) {
		def buffer = new byte[bufferSize]

		file.withDataInputStream { stream ->
			[1..times].any {
				def count = stream.read buffer

				checkChunk buffer, count
			}
		}
	}

	private boolean checkEachByte(byte[] buffer, int count, Closure checkByte) {
		int lastByteTranslated = 0

		for (int i in 0..count - 1) {
			int unsignedByte = buffer[i] & 0xff
			int utf8value = lastByteTranslated + unsignedByte
			lastByteTranslated = unsignedByte << 8

			if (checkByte(unsignedByte, utf8value)) {
				return true
			}

			false
		}
	}

	private boolean isCharacterText(int unsignedByte, int utf8value) {
		unsignedByte in [TAB_CHARACTER, LINE_FEED_CHARACTER, FORM_FEED_CHARACTER, CARRIAGE_RETURN_CHARACTER] ||
				(unsignedByte >= ASCII_TEXT_SYMBOLS_LOWER_BOUND && unsignedByte <= ASCII_TEXT_SYMBOLS_UPPER_BOUND) ||
				(unsignedByte >= LATIN_CHARSET_LOWER_BOUND && unsignedByte <= LATIN_CHARSET_UPPER_BOUND) ||
				(utf8value >= LATIN_IN_UTF_8_LOWER_BOUND && utf8value <= LATIN_IN_UTF_8_UPPER_BOUND)
	}

}
