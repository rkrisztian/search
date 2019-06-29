package search.colors

class HtmlColors {

	// =================
	// Foreground colors
	// =================
	private static final String RED = 'color: #FF0000'

	private static final String WHITE = 'color: #FFFFFF'

	private static final String MAGENTA = 'color: #FF00FF'

	private static final String GREEN = 'color: #179C0E'

	private static final String CYAN = 'color: #18B2B2'

	// =================
	// Background colors
	// =================
	private static final String ON_RED = 'background-color: #FF0000'

	private static final String ON_GREEN = 'background-color: #008000'

	// =================
	// Font effects
	// =================
	private static final String BOLD = 'font-weight: bold'

	private static final EnumMap<ColorType, List<String>> colorMappings = [
			(ColorType.MATCH_COLOR)                             : [BOLD, RED],
			(ColorType.REPLACE_COLOR)                           : [ON_RED, WHITE],
			(ColorType.DRY_REPLACE_COLOR)                       : [ON_GREEN, WHITE],
			(ColorType.FILE_PATH_COLOR)                         : [BOLD, MAGENTA],
			(ColorType.LINE_NUMBER_COLOR)                       : [GREEN],
			(ColorType.SKIPPED_LINES_MARKER_COLOR)              : [GREEN],
			(ColorType.CONTEXT_LINES_COLOR)                     : [CYAN],
			(ColorType.CONTEXT_LINES_SKIPPED_LINES_MARKER_COLOR): [BOLD, CYAN],
			(ColorType.ERROR_COLOR)                             : [RED],
			(ColorType.WARNING_COLOR)                           : [MAGENTA]
	]

	private boolean disableColors

	HtmlColors(boolean disableColors) {
		this.disableColors = disableColors
	}

	Map<String, String> format(ColorType color) {
		if (disableColors) {
			return [:]
		}

		['style': colorMappings[color].join('; ')]
	}

}
