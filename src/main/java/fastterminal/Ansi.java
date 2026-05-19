package fastterminal;

/**
 * @class Ansi
 * @brief High-performance ANSI escape codes and VT100 control sequences.
 * 
 * Provides static constants and utility operations for toggling alternative buffers,
 * cursor visibility, and generating 24-bit True Color sequences with zero-garbage collection overhead.
 */
public final class Ansi {

    /** @brief Escape code to enter the alternative screen buffer. */
    public static final String ENTER_ALT_BUFFER = "\033[?1049h";
    
    /** @brief Escape code to exit the alternative screen buffer. */
    public static final String EXIT_ALT_BUFFER  = "\033[?1049l";

    /** @brief Escape code to hide the terminal cursor. */
    public static final String HIDE_CURSOR      = "\033[?25l";
    
    /** @brief Escape code to show the terminal cursor. */
    public static final String SHOW_CURSOR      = "\033[?25h";

    /** @brief Escape code to clear the terminal screen buffer. */
    public static final String CLEAR_SCREEN     = "\033[2J";
    
    /** @brief Escape code to move the terminal cursor home (1,1). */
    public static final String CURSOR_HOME      = "\033[1;1H";

    /** @brief Escape code to reset all formatting styles and colors. */
    public static final String RESET            = "\033[0m";
    
    /** @brief Escape code to restore the default foreground color. */
    public static final String DEFAULT_FG       = "\033[39m";
    
    /** @brief Escape code to restore the default background color. */
    public static final String DEFAULT_BG       = "\033[49m";

    /** @brief Prefix for ANSI 24-bit True Color foreground escapes. */
    public static final String RGB_FG_PREFIX    = "\033[38;2;";
    
    /** @brief Prefix for ANSI 24-bit True Color background escapes. */
    public static final String RGB_BG_PREFIX    = "\033[48;2;";
    
    /** @brief Suffix for ANSI color control escape formatting. */
    public static final String RGB_SUFFIX       = "m";

    private Ansi() {
        // Prevent instantiation
    }

    /**
     * @brief Utility method to print multiple escape codes sequentially and flush standard output.
     * 
     * Useful for safe atomic console initialization and teardown sequences.
     * 
     * @param codes Varargs array of escape sequence strings.
     */
    public static void print(String... codes) {
        for (String code : codes) {
            System.out.print(code);
        }
        System.out.flush();
    }
}
