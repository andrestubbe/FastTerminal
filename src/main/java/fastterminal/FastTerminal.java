package fastterminal;

import fastcore.FastCore;
import java.io.File;

/**
 * FastTerminal Main API Class.
 * Native high-performance console/terminal capabilities.
 */
public class FastTerminal {

    static {
        try {
            // First, try loading natively from local build folder for deterministic dev cycles
            File localDll = new File("build/fastterminal.dll");
            if (!localDll.exists()) {
                localDll = new File("../../build/fastterminal.dll");
            }
            
            if (localDll.exists()) {
                System.load(localDll.getAbsolutePath());
            } else {
                // Fallback to standard classpath packaging extraction
                FastCore.loadLibrary("fastterminal");
            }
        } catch (Throwable e) {
            // Ultimate fallback in case load fails
            try {
                FastCore.loadLibrary("fastterminal");
            } catch (Throwable ignored) {}
        }
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
