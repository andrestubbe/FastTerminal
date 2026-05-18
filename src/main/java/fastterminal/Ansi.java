package fastterminal;

/**
 * High-performance, clean-code ANSI escape codes and VT100 control sequences.
 * Zero-garbage collection design for maximum rendering throughput.
 */
public final class Ansi {

    // Alternate Screen Buffer Controls
    public static final String ENTER_ALT_BUFFER = "\033[?1049h";
    public static final String EXIT_ALT_BUFFER  = "\033[?1049l";

    // Cursor Visibility Controls
    public static final String HIDE_CURSOR      = "\033[?25l";
    public static final String SHOW_CURSOR      = "\033[?25h";

    // Screen and Cursor Position Controls
    public static final String CLEAR_SCREEN     = "\033[2J";
    public static final String CURSOR_HOME      = "\033[1;1H";

    // Color and Style Reset Controls
    public static final String RESET            = "\033[0m";
    public static final String DEFAULT_FG       = "\033[39m";
    public static final String DEFAULT_BG       = "\033[49m";

    private Ansi() {
        // Prevent instantiation
    }

    /**
     * Utility method to print multiple escape codes sequentially and flush standard output.
     * Useful for setup and teardown of console state.
     */
    public static void print(String... codes) {
        for (String code : codes) {
            System.out.print(code);
        }
        System.out.flush();
    }
}
