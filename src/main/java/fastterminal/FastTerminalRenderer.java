package fastterminal;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * High-performance viewport compositor and ANSI rendering engine.
 * Leverages state-minimizing ANSI code emitters for 24-bit True Colors.
 */
public final class FastTerminalRenderer {

    private List<FastTerminalScene> scenes = new ArrayList<>();
    private final int width;
    private final int height;
    
    // Compositing buffers
    private int[] compositeCodepoints;
    private int[] compositeFg;
    private int[] compositeBg;

    public FastTerminalRenderer(final int width, final int height) {
        this.width = width;
        this.height = height;
        this.compositeCodepoints = new int[width * height];
        this.compositeFg = new int[width * height];
        this.compositeBg = new int[width * height];
        this.clear();
    }

    public void addScene(final FastTerminalScene scene) {
        this.scenes.add(scene);
    }

    public void render() {
        this.clear();
        
        // Composite all layers
        for (final FastTerminalScene scene : this.scenes) {
            if (scene.isDirty()) {
                scene.update();
                scene.setDirty(false);
            }
            this.insertScene(scene);
        }
        
        // Print home command to avoid full console clear flash
        System.out.print(Ansi.CURSOR_HOME);

        // Build the optimized ANSI True Color rendering buffer
        StringBuilder sb = new StringBuilder(width * height * 16);
        int currentFg = -2;
        int currentBg = -2;

        for (int i = 0; i < compositeCodepoints.length; i++) {
            int cp = compositeCodepoints[i];
            int fg = compositeFg[i];
            int bg = compositeBg[i];

            if (cp == -99) {
                // Wide character continuation cell - skip printing the character
                // but we MUST still perform the row split check!
                if ((i + 1) % this.width == 0 && (i + 1) < compositeCodepoints.length) {
                    sb.append(Ansi.RESET).append("\n");
                    currentFg = -2;
                    currentBg = -2;
                }
                continue;
            }

            // Optimize foreground escape codes
            if (fg != currentFg) {
                if (fg == -1) {
                    sb.append(Ansi.DEFAULT_FG);
                } else {
                    int r = (fg >> 16) & 0xFF;
                    int g = (fg >> 8) & 0xFF;
                    int b = fg & 0xFF;
                    sb.append("\033[38;2;").append(r).append(";").append(g).append(";").append(b).append("m");
                }
                currentFg = fg;
            }

            // Optimize background escape codes
            if (bg != currentBg) {
                if (bg == -1) {
                    sb.append(Ansi.DEFAULT_BG);
                } else {
                    int r = (bg >> 16) & 0xFF;
                    int g = (bg >> 8) & 0xFF;
                    int b = bg & 0xFF;
                    sb.append("\033[48;2;").append(r).append(";").append(g).append(";").append(b).append("m");
                }
                currentBg = bg;
            }

            // Write the codepoint safely
            if (Character.isValidCodePoint(cp)) {
                sb.appendCodePoint(cp);
            } else {
                sb.append(' ');
            }

            // Explicitly force newline at the end of each row
            if ((i + 1) % this.width == 0 && (i + 1) < compositeCodepoints.length) {
                sb.append(Ansi.RESET).append("\n"); // Reset colors before newline to avoid bleeding
                currentFg = -2;
                currentBg = -2;
            }
        }

        // Reset all console attributes at the end of the frame
        sb.append(Ansi.RESET);

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        System.out.write(bytes, 0, bytes.length);
        System.out.flush();
    }

    private void insertScene(final FastTerminalScene scene) {
        final int[] srcCodepoints = scene.getCodepointBuffer();
        final int[] srcFg = scene.getFgBuffer();
        final int[] srcBg = scene.getBgBuffer();
        final int srcWidth = scene.getWidth();
        final int srcHeight = scene.getHeight();
        final int dstX = scene.getX();
        final int dstY = scene.getY();

        for (int row = 0; row < srcHeight; row++) {
            final int dstRow = dstY + row;
            if (dstRow < 0 || dstRow >= this.height) continue;
            
            int srcPos = row * srcWidth;
            int dstPos = dstRow * this.width + dstX;
            int length = srcWidth;

            if (dstX < 0) {
                srcPos -= dstX;
                length += dstX;
                dstPos = dstRow * this.width;
            }
            if (dstX + length > this.width) {
                length = this.width - dstX;
            }
            if (length <= 0) continue;

            System.arraycopy(srcCodepoints, srcPos, this.compositeCodepoints, dstPos, length);
            System.arraycopy(srcFg, srcPos, this.compositeFg, dstPos, length);
            System.arraycopy(srcBg, srcPos, this.compositeBg, dstPos, length);
        }
    }

    public void clear() {
        Arrays.fill(this.compositeCodepoints, ' ');
        Arrays.fill(this.compositeFg, -1);
        Arrays.fill(this.compositeBg, -1);
    }

    public void dispose() {
        if (this.scenes != null) {
            this.scenes.clear();
            this.scenes = null;
        }
        this.compositeCodepoints = null;
        this.compositeFg = null;
        this.compositeBg = null;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
