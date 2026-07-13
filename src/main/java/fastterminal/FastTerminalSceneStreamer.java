package fastterminal;

import fastansi.FastANSI;

/**
 * Utility for converting a FastTerminalScene into a sequential ANSI string block.
 * This is incredibly useful for rendering TUI components within standard 
 * scrolling CLI application logs (like chat agent outputs) without requiring 
 * a full-screen, double-buffered engine.
 */
public class FastTerminalSceneStreamer {

    private static final String RGB_FG_PREFIX = FastANSI.CSI + "38;2;";
    private static final String RGB_BG_PREFIX = FastANSI.CSI + "48;2;";
    
    /**
     * Converts the entire scene into an ANSI-formatted string.
     * Each row in the scene is separated by a newline character.
     *
     * @param scene The mini-scene containing rendered components.
     * @return A standard string with ANSI escape codes and line breaks.
     */
    public static String sceneToAnsiStream(FastTerminalScene scene) {
        StringBuilder sb = new StringBuilder();
        int[] cpBuf = scene.getCodepointBuffer();
        int[] fgBuf = scene.getFgBuffer();
        int[] bgBuf = scene.getBgBuffer();
        
        int width = scene.getWidth();
        int height = scene.getHeight();
        
        int currentFg = -2;
        int currentBg = -2;
        
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                int i = r * width + c;
                int cp = cpBuf[i];
                int fg = fgBuf[i];
                int bg = bgBuf[i];
                
                if (cp == -99) continue; // Native continuation cell marker
                
                // Emit Foreground if changed
                if (fg != currentFg) {
                    if (fg == -1) {
                        sb.append(FastANSI.FG_DEFAULT);
                    } else {
                        sb.append(RGB_FG_PREFIX)
                          .append((fg >>> 16) & 0xFF).append(';')
                          .append((fg >>> 8) & 0xFF).append(';')
                          .append(fg & 0xFF).append('m');
                    }
                    currentFg = fg;
                }
                
                // Emit Background if changed
                if (bg != currentBg) {
                    if (bg == -1) {
                        sb.append(FastANSI.BG_DEFAULT);
                    } else {
                        sb.append(RGB_BG_PREFIX)
                          .append((bg >>> 16) & 0xFF).append(';')
                          .append((bg >>> 8) & 0xFF).append(';')
                          .append(bg & 0xFF).append('m');
                    }
                    currentBg = bg;
                }
                
                // Append character (fallback to space if 0)
                if (cp == 0) {
                    sb.append(' ');
                } else if (Character.isValidCodePoint(cp)) {
                    sb.appendCodePoint(cp);
                } else {
                    sb.append(' ');
                }
            }
            // Reset styles and break line
            sb.append(FastANSI.RESET).append('\n');
            currentFg = -2;
            currentBg = -2;
        }
        
        return sb.toString();
    }
}
