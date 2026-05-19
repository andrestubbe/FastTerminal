package fastterminal;

import fastcore.FastCore;
import java.io.File;

/**
 * @class FastTerminal
 * @brief FastTerminal main native JNI boundary and capabilities manager.
 * 
 * Interacts with Win32 backend console interfaces to query terminal dimensions, 
 * configure raw console flags, track window coordinate offsets, focus states, and mouse bounds.
 */
public class FastTerminal {

    static {
        FastCore.loadLibrary("fastterminal");
    }

    /**
     * @brief Queries the dynamic dimensions of the active console screen buffer.
     * 
     * @return jintArray containing [columns, rows], or null on JNI query failures.
     */
    public static native int[] getTerminalSize();

    /**
     * @brief Query dynamic console buffer dimensions with a safe premium fallback.
     * 
     * @param defaultCols Standard fallback column width.
     * @param defaultRows Standard fallback row height.
     * @return 2-element array containing [cols, rows].
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
     * @brief Configures standard Win32 console mode flags (toggling raw input modes).
     * 
     * @param enableRaw True to enable direct non-blocking raw mode, false to restore default console buffer flags.
     */
    public static native void setRawMode(boolean enableRaw);

    /**
     * @brief Retrieves detailed hardware rect boundaries, client offsets, and console font character cell sizes.
     * 
     * Indexes:
     * - [0]: rect.left (pixels)
     * - [1]: rect.top (pixels)
     * - [2]: clientOffset.x (pixels)
     * - [3]: clientOffset.y (pixels)
     * - [4]: fontWidth (pixels)
     * - [5]: fontHeight (pixels)
     * - [6]: clientWidth (pixels)
     * - [7]: clientHeight (pixels)
     * 
     * @return int array containing hardware console window info metrics.
     */
    public static native int[] getConsoleWindowInfo();

    /**
     * @brief Checks if our console window or parent host currently holds focus in Windows.
     * 
     * Walks owner and ancestor chains, making it fully compatible with modern host terminals like wt.exe.
     * 
     * @return True if terminal is focused, False otherwise.
     */
    public static native boolean isTerminalFocused();

    /**
     * @brief Determines if the OS mouse cursor is currently hovering within terminal boundaries.
     * 
     * @return True if hovering, False otherwise.
     */
    public static native boolean isMouseOverTerminal();
}
