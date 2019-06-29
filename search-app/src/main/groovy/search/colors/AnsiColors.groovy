package search.colors

class AnsiColors {

	// =================
	// Foreground colors
	// =================
	private static final String RED = '\u001B[31m'

	private static final String WHITE = '\u001B[37m'

	private static final String MAGENTA = '\u001B[35m'

	private static final String GREEN = '\u001B[32m'

	private static final String CYAN = '\u001B[36m'

	// =================
	// Background colors
	// =================
	private static final String ON_RED = '\u001B[41m'

	private static final String ON_GREEN = '\u001B[42m'

	// =================
	// Font effects
	// =================
	private static final String BOLD = '\u001B[1m'

	// =================
	// Special
	// =================
	public static final String RESET = '\u001B[0m'

	private static final EnumMap<ColorType, String> colorMappings = [
			(ColorType.MATCH_COLOR)                             : BOLD + RED,
			(ColorType.REPLACE_COLOR)                           : ON_RED + WHITE,
			(ColorType.DRY_REPLACE_COLOR)                       : ON_GREEN + WHITE,
			(ColorType.FILE_PATH_COLOR)                         : BOLD + MAGENTA,
			(ColorType.LINE_NUMBER_COLOR)                       : GREEN,
			(ColorType.SKIPPED_LINES_MARKER_COLOR)              : GREEN,
			(ColorType.CONTEXT_LINES_COLOR)                     : CYAN,
			(ColorType.CONTEXT_LINES_SKIPPED_LINES_MARKER_COLOR): BOLD + CYAN,
			(ColorType.ERROR_COLOR)                             : RED,
			(ColorType.WARNING_COLOR)                           : MAGENTA
	]

	private boolean disableColors

	AnsiColors(boolean disableColors) {
		this.disableColors = disableColors
	}

	String format(ColorType color, message) {
		if (disableColors) {
			return message
		}

		colorMappings[color] + message + RESET
	}

}
