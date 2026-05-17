package fastterminal;

import fastcore.FastCore;

/**
 * FastTerminal Main API Class.
 * Native high-performance console/terminal capabilities.
 */
public class FastTerminal {

    static {
        // Automatically extracts and loads the native library
        FastCore.loadLibrary("fastterminal");
    }

    /**
     * Returns the dynamic dimensions of the active console screen buffer.
     * [0] = Width (Columns), [1] = Height (Rows)
     */
    public static native int[] getTerminalSize();

    /**
     * Configures the console mode (e.g. Raw Mode, input/output flags).
     * @param enableRaw true to enable raw non-blocking mode, false to restore
     */
    public static native void setRawMode(boolean enableRaw);
}
