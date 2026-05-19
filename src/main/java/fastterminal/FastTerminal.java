package fastterminal;

import fastcore.FastCore;
import java.io.File;

/**
 * FastTerminal Main API Class.
 * Native high-performance console/terminal capabilities.
 */
public class FastTerminal {

    static {
        FastCore.loadLibrary("fastterminal");
    }

    /**
     * Returns the dynamic dimensions of the active console screen buffer.
     * [0] = Width (Columns), [1] = Height (Rows)
     */
    public static native int[] getTerminalSize();

    /**
     * Helper to query dynamic console dimensions with a safe fallback.
     * @param defaultCols standard fallback columns
     * @param defaultRows standard fallback rows
     * @return 2-element array: [0] = Width (Columns), [1] = Height (Rows)
     */
    public static int[] getWindowSize(int defaultCols, int defaultRows) {
        try {
            int[] size = getTerminalSize();
            if (size != null && size[0] > 0 && size[1] > 0) {
                return size;
            }
        } catch (Throwable ignored) {
        }
        return new int[]{defaultCols, defaultRows};
    }

    /**
     * Configures the console mode (e.g. Raw Mode, input/output flags).
     * @param enableRaw true to enable raw non-blocking mode, false to restore
     */
    public static native void setRawMode(boolean enableRaw);

    /**
     * Retrieves the console window's screen rect, client offset, and font character cell size.
     * [0] = rect.left, [1] = rect.top, [2] = clientOffset.x, [3] = clientOffset.y, [4] = fontWidth, [5] = fontHeight
     */
    public static native int[] getConsoleWindowInfo();

    /**
     * Checks if our console window is the active focused window in Windows.
     */
    public static native boolean isTerminalFocused();

    /**
     * Checks if the mouse cursor is hovering over our terminal window.
     */
    public static native boolean isMouseOverTerminal();
}
