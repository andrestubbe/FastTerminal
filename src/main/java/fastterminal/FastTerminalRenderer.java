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
    private int width;
    private int height;

    // Compositing buffers
    private int[] compositeCodepoints;
    private int[] compositeFg;
    private int[] compositeBg;

    // Double-buffering state for diff rendering
    private int[] prevCodepoints;
    private int[] prevFg;
    private int[] prevBg;
    private boolean forceFullRedraw = true;
    private boolean diffRenderingEnabled = true;

    public FastTerminalRenderer(final int width, final int height) {
        this.width = width;
        this.height = height;
        this.compositeCodepoints = new int[width * height];
        this.compositeFg = new int[width * height];
        this.compositeBg = new int[width * height];
        this.prevCodepoints = new int[width * height];
        this.prevFg = new int[width * height];
        this.prevBg = new int[width * height];
        this.clear();
        this.clearPrev();
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

        // Build the optimized ANSI True Color rendering buffer
        StringBuilder sb = new StringBuilder(width * height * 16);
        int currentFg = -2;
        int currentBg = -2;

        if (!this.diffRenderingEnabled || this.forceFullRedraw) {
            // Full Redraw mode
            // Print home command to avoid full console clear flash
            System.out.print(Ansi.CURSOR_HOME);

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
                        sb.append(Ansi.RGB_FG_PREFIX).append(r).append(";").append(g).append(";").append(b)
                                .append(Ansi.RGB_SUFFIX);
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
                        sb.append(Ansi.RGB_BG_PREFIX).append(r).append(";").append(g).append(";").append(b)
                                .append(Ansi.RGB_SUFFIX);
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

            sb.append(Ansi.RESET);
            this.forceFullRedraw = false;
        } else {
            // Diff-Rendering / Double-Buffering mode
            int expectedPos = -1;

            for (int i = 0; i < compositeCodepoints.length; i++) {
                int cp = compositeCodepoints[i];
                int fg = compositeFg[i];
                int bg = compositeBg[i];

                int prevCp = prevCodepoints[i];
                int prevFgVal = prevFg[i];
                int prevBgVal = prevBg[i];

                // If nothing changed, we skip this cell!
                if (cp == prevCp && fg == prevFgVal && bg == prevBgVal) {
                    continue;
                }

                // Wide character continuation cell check
                if (cp == -99) {
                    continue;
                }

                // 1. Position cursor if we skipped cells
                if (i != expectedPos) {
                    int row = i / this.width;
                    int col = i % this.width;
                    sb.append("\033[").append(row + 1).append(";").append(col + 1).append("H");
                }

                // 2. Output foreground escape code
                if (fg != currentFg) {
                    if (fg == -1) {
                        sb.append(Ansi.DEFAULT_FG);
                    } else {
                        int r = (fg >> 16) & 0xFF;
                        int g = (fg >> 8) & 0xFF;
                        int b = fg & 0xFF;
                        sb.append(Ansi.RGB_FG_PREFIX).append(r).append(";").append(g).append(";").append(b)
                                .append(Ansi.RGB_SUFFIX);
                    }
                    currentFg = fg;
                }

                // 3. Output background escape code
                if (bg != currentBg) {
                    if (bg == -1) {
                        sb.append(Ansi.DEFAULT_BG);
                    } else {
                        int r = (bg >> 16) & 0xFF;
                        int g = (bg >> 8) & 0xFF;
                        int b = bg & 0xFF;
                        sb.append(Ansi.RGB_BG_PREFIX).append(r).append(";").append(g).append(";").append(b)
                                .append(Ansi.RGB_SUFFIX);
                    }
                    currentBg = bg;
                }

                // 4. Output the codepoint safely
                if (Character.isValidCodePoint(cp)) {
                    sb.appendCodePoint(cp);
                } else {
                    sb.append(' ');
                }

                // Set natural next cursor position
                expectedPos = i + 1;
                // If we are at the end of the row, force a manual jump next time to be safe
                if (expectedPos % this.width == 0) {
                    expectedPos = -1;
                }
            }

            // Always reset styles at the end of the diff updates
            sb.append(Ansi.RESET);
        }

        // Keep prev buffers in sync with current composite frame
        System.arraycopy(this.compositeCodepoints, 0, this.prevCodepoints, 0, this.compositeCodepoints.length);
        System.arraycopy(this.compositeFg, 0, this.prevFg, 0, this.compositeFg.length);
        System.arraycopy(this.compositeBg, 0, this.prevBg, 0, this.compositeBg.length);

        // Blit the built buffer to standard output
        if (sb.length() > 0) {
            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            System.out.write(bytes, 0, bytes.length);
            System.out.flush();
        }
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
            if (dstRow < 0 || dstRow >= this.height)
                continue;

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
            if (length <= 0)
                continue;

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

    /**
     * Resizes the compositing and previous buffers dynamically in-place.
     * 
     * @return true if a resize actually occurred, false otherwise.
     */
    public boolean resize(final int newWidth, final int newHeight) {
        if (newWidth <= 0 || newHeight <= 0)
            return false;
        if (newWidth == this.width && newHeight == this.height)
            return false;

        this.width = newWidth;
        this.height = newHeight;

        this.compositeCodepoints = new int[newWidth * newHeight];
        this.compositeFg = new int[newWidth * newHeight];
        this.compositeBg = new int[newWidth * newHeight];

        this.prevCodepoints = new int[newWidth * newHeight];
        this.prevFg = new int[newWidth * newHeight];
        this.prevBg = new int[newWidth * newHeight];

        this.clear();
        this.clearPrev();
        return true;
    }

    public void dispose() {
        if (this.scenes != null) {
            this.scenes.clear();
            this.scenes = null;
        }
        this.compositeCodepoints = null;
        this.compositeFg = null;
        this.compositeBg = null;
        this.prevCodepoints = null;
        this.prevFg = null;
        this.prevBg = null;
    }

    public void clearPrev() {
        if (this.prevCodepoints != null) {
            Arrays.fill(this.prevCodepoints, ' ');
            Arrays.fill(this.prevFg, -1);
            Arrays.fill(this.prevBg, -1);
        }
        this.forceFullRedraw = true;
    }

    public boolean isDiffRenderingEnabled() {
        return this.diffRenderingEnabled;
    }

    public void setDiffRenderingEnabled(boolean enabled) {
        this.diffRenderingEnabled = enabled;
        if (!enabled) {
            this.forceFullRedraw = true;
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
