package fastterminal;

import fastcore.FastCore;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @class FastTerminal
 * @brief FastTerminal main native JNI boundary and capabilities manager.
 * <p>
 * Interacts with Win32 backend console interfaces to query terminal dimensions,
 * configure raw console flags, track window coordinate offsets, focus states, and mouse bounds.
 */
public class FastTerminal {

    static {
        FastCore.loadLibrary("fastterminal");
    }

    private static final List<ResizeListener> resizeListeners = new CopyOnWriteArrayList<>();

    public interface ResizeListener {
        void onResize(int cols, int rows);
    }

    /**
     * @return int[] Flat snapshot array, or [0, 0] on failure.
     * @brief Reads the visible console screen buffer as a snapshot.
     * <p>
     * Returns a flat int array with layout: [cols, rows, cp0, fg0, bg0, cp1, fg1, bg1, ...]
     * where each cell is a Unicode codepoint + 24-bit RGB foreground + 24-bit RGB background.
     * Total length = 2 + cols * rows * 3.
     * <p>
     * Returns [0, 0] if the read fails (e.g. running inside Windows Terminal's pseudo-console).
     */
    public static native int[] readConsoleOutput();

    /**
     * @param defaultCols Standard fallback column width.
     * @param defaultRows Standard fallback row height.
     * @return FastTerminalScene populated with the snapshot, or a transparent scene on failure.
     * @brief Captures the current visible console screen buffer as a FastTerminalScene.
     */
    public static FastTerminalScene captureScreen(int defaultCols, int defaultRows) {
        int w = defaultCols;
        int h = defaultRows;
        try {
            int[] size = getTerminalSize();
            if (size != null && size[0] > 0 && size[1] > 0) {
                w = size[0];
                h = size[1];
            }
        } catch (Throwable ignored) {
        }

        FastTerminalScene scene = new FastTerminalScene(0, 0, w, h);
        try {
            int[] snap = readConsoleOutput();
            if (snap != null && snap.length >= 2 && snap[0] > 0 && snap[1] > 0) {
                int sC = snap[0], sR = snap[1];
                int uC = Math.min(sC, w), uR = Math.min(sR, h);
                for (int r = 0; r < uR; r++) {
                    for (int c = 0; c < uC; c++) {
                        int b = 2 + (r * sC + c) * 3;
                        scene.writeCell(c, r, snap[b], snap[b + 1], snap[b + 2]);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return scene;
    }

    /**
     * @brief Registers a ResizeListener to handle terminal resizing.
     * <p>
     * Starts the native event-driven resize watcher thread when the first listener is added.
     * 
     * @param listener The listener to add.
     */
    public static void addResizeListener(ResizeListener listener) {
        resizeListeners.add(listener);
        if (resizeListeners.size() == 1) {
            try {
                startNativeResizeWatcher();
            } catch (UnsatisfiedLinkError e) {
                System.err.println("[FastTerminal] Native resize watcher not available: " + e.getMessage());
            }
        }
    }

    /**
     * @brief Unregisters a ResizeListener.
     * <p>
     * Stops the native event-driven resize watcher thread when the last listener is removed.
     * 
     * @param listener The listener to remove.
     */
    public static void removeResizeListener(ResizeListener listener) {
        resizeListeners.remove(listener);
        if (resizeListeners.isEmpty()) {
            try {
                stopNativeResizeWatcher();
            } catch (UnsatisfiedLinkError ignored) {
            }
        }
    }

    /**
     * @brief JNI callback method triggered natively on console buffer resize.
     * <p>
     * Dispatches resize events to all registered listeners.
     * 
     * @param cols New terminal columns width.
     * @param rows New terminal rows height.
     */
    public static void nativeResizeCallback(int cols, int rows) {
        for (ResizeListener l : resizeListeners) {
            try {
                l.onResize(cols, rows);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * @brief Starts the native background resize watcher thread (JNI).
     * <p>
     * Intercepts WINDOW_BUFFER_SIZE_EVENTs on a second CONIN$ handle to avoid stealing key inputs.
     */
    private static native void startNativeResizeWatcher();

    /**
     * @brief Stops the native background resize watcher thread (JNI).
     */
    private static native void stopNativeResizeWatcher();

    /**
     * @return True if hovering, False otherwise.
     * @brief Determines if the OS mouse cursor is currently hovering within terminal boundaries.
     */
    public static native boolean isMouseOverTerminal();

    /**
     * @return True if terminal is focused, False otherwise.
     * @brief Checks if our console window or parent host currently holds focus in Windows.
     * <p>
     * Walks owner and ancestor chains, making it fully compatible with modern host terminals like wt.exe.
     */
    public static native boolean isTerminalFocused();

    /**
     * @return int array containing hardware console window info metrics.
     * @brief Retrieves detailed hardware rect boundaries, client offsets, and console font character cell sizes.
     * <p>
     * Indexes:
     * - [0]: rect.left (pixels)
     * - [1]: rect.top (pixels)
     * - [2]: clientOffset.x (pixels)
     * - [3]: clientOffset.y (pixels)
     * - [4]: fontWidth (pixels)
     * - [5]: fontHeight (pixels)
     * - [6]: clientWidth (pixels)
     * - [7]: clientHeight (pixels)
     */
    public static native int[] getConsoleWindowInfo();

    /**
     * @return int[] [col, row], or [0, 0] on failure.
     * @brief Returns the current console cursor position as [col, row] (0-based).
     */
    public static native int[] getCursorPosition();

    /**
     * @return jintArray containing [columns, rows], or null on JNI query failures.
     * @brief Queries the dynamic dimensions of the active console screen buffer.
     */
    public static native int[] getTerminalSize();

    /**
     * @param defaultCols Standard fallback column width.
     * @param defaultRows Standard fallback row height.
     * @return 2-element array containing [cols, rows].
     * @brief Query dynamic console buffer dimensions with a safe premium fallback.
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
     * @param enableRaw True to enable direct non-blocking raw mode with VT input, false to restore defaults.
     * @brief Configures high-precision Virtual Terminal raw modes for standard input/output.
     */
    public static native void setAnsiRawMode(boolean enableRaw);

    /**
     * @param enableRaw True to enable direct non-blocking raw mode, false to restore default console buffer flags.
     * @brief Configures standard Win32 console mode flags (toggling raw input modes).
     */
    public static native void setRawMode(boolean enableRaw);

    /**
     * @param title The title to display in the terminal window frame.
     * @brief Sets the console window title string.
     */
    public static native void setTitle(String title);

    /**
     * @param visible True to show the standard system mouse cursor, False to hide it.
     * @brief Toggles system mouse pointer cursor visibility globally.
     */
    public static native void setSystemCursorVisible(boolean visible);
}
