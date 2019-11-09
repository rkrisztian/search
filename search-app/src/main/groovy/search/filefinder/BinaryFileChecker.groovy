package search.filefinder

import groovy.transform.CompileStatic

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
@CompileStatic
class BinaryFileChecker {

	private static final int TAB_CHAR = 0x09
	private static final int LINE_FEED_CHAR = 0x0A
	private static final int FORM_FEED_CHAR = 0x0C
	private static final int CARRIAGE_RETURN_CHAR = 0x0D
	private static final int ASCII_TEXT_START = 0x20
	private static final int ASCII_TEXT_END = 0x7E
	private final static int LATIN_CHARSET_START = 0xA0
	private final static int LATIN_CHARSET_END = 0xEE
	private final static int LATIN_IN_UTF_8_START = 0x2E2E
	private static final int LATIN_IN_UTF_8_END = 0xC3BF

	static boolean checkIfBinary(File file) {
		checkEachChunk(file, 512, 1) { byte[] buffer, int count ->
			checkEachByte(buffer, count) { int unsignedByte, int utf8value ->
				!isCharacterText(unsignedByte, utf8value)
			}
		}
	}

	private static boolean checkEachChunk(File file, int bufferSize, int times, Closure checkChunk) {
		def buffer = new byte[bufferSize]

		file.withDataInputStream { stream ->
			[1..times].any {
				def count = stream.read buffer

				checkChunk buffer, count
			}
		}
	}

	private static boolean checkEachByte(byte[] buffer, int count, Closure checkByte) {
		int lastByteTranslated = 0

		for (int i in 0..count - 1) {
			int unsignedByte = buffer[i] & 0xff
			int utf8value = lastByteTranslated + unsignedByte
			lastByteTranslated = unsignedByte << 8

			if (checkByte(unsignedByte, utf8value)) {
				return true
			}
		}

		false
	}

	private static boolean isCharacterText(int unsignedByte, int utf8value) {
		if (unsignedByte == 0) {
			return false
		}

		unsignedByte in [TAB_CHAR, LINE_FEED_CHAR, FORM_FEED_CHAR, CARRIAGE_RETURN_CHAR] ||
				(unsignedByte >= ASCII_TEXT_START && unsignedByte <= ASCII_TEXT_END) ||
				(unsignedByte >= LATIN_CHARSET_START && unsignedByte <= LATIN_CHARSET_END) ||
				(utf8value >= LATIN_IN_UTF_8_START && utf8value <= LATIN_IN_UTF_8_END) ||
				Character.isLetterOrDigit(utf8value)
	}

}
