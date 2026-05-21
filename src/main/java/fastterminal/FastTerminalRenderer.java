package fastterminal;

import fastansi.FastANSI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @class FastTerminalRenderer
 * @brief High-performance double-buffered ANSI rendering and screen compositing engine.
 *
 * Composites multiple FastTerminalScene layers and converts screen diff state transitions
 * to optimized 24-bit True Color ANSI escape streams.
 *
 * Rendering pipeline per frame:
 *   clear → compositeScenes → choose strategy → syncPrevBuffers → flushOutput
 *
 * Strategies (in priority order):
 *   1. Dirty-rectangles  — when dirtyRectanglesEnabled + diff mode + no forced redraw
 *   2. Full redraw       — when diffRenderingEnabled=false or forceFullRedraw=true
 *   3. Diff / double-buffer — default; only changed cells are emitted
 */
public final class FastTerminalRenderer {

    // ── ANSI escape fragments ────────────────────────────────────────────────
    // RGB_FG/BG prefixes are kept as local constants because FastANSI.fg(r,g,b)
    // and FastANSI.bg(r,g,b) allocate a new String per call — too expensive for
    // a hot render loop. We append directly to the StringBuilder instead.
    private static final String RGB_FG_PREFIX = FastANSI.CSI + "38;2;";
    private static final String RGB_BG_PREFIX = FastANSI.CSI + "48;2;";
    private static final String RGB_SUFFIX    = "m";

    // ── Scene registry ───────────────────────────────────────────────────────
    private List<FastTerminalScene> scenes = new ArrayList<>();

    // ── Dimensions ──────────────────────────────────────────────────────────
    private int width;
    private int height;

    // ── Composite buffers (current frame) ───────────────────────────────────
    private int[] compositeCodepoints;
    private int[] compositeFg;
    private int[] compositeBg;

    // ── Prev buffers (last flushed frame, for diff) ──────────────────────────
    private int[] prevCodepoints;
    private int[] prevFg;
    private int[] prevBg;

    // ── Rendering state ──────────────────────────────────────────────────────
    private boolean forceFullRedraw        = true;
    private boolean diffRenderingEnabled   = true;
    private boolean dirtyRectanglesEnabled = false;
    private int     lastFlushedBytes       = 0;

    // ── Reused per-frame output buffer (avoids per-frame allocation) ─────────
    private final StringBuilder sb;

    // ════════════════════════════════════════════════════════════════════════
    // Construction / lifecycle
    // ════════════════════════════════════════════════════════════════════════

    /**
     * @brief Allocates all double-buffered structures.
     * @param width  Initial terminal column count.
     * @param height Initial terminal row count.
     */
    public FastTerminalRenderer(final int width, final int height) {
        this.width  = width;
        this.height = height;
        int cells = width * height;
        this.compositeCodepoints = new int[cells];
        this.compositeFg         = new int[cells];
        this.compositeBg         = new int[cells];
        this.prevCodepoints      = new int[cells];
        this.prevFg              = new int[cells];
        this.prevBg              = new int[cells];
        this.sb = new StringBuilder(cells * 20);
        clear();
        clearPrev();
    }

    /** @brief Registers an overlay scene layer. */
    public void addScene(final FastTerminalScene scene) {
        this.scenes.add(scene);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Main render entry point
    // ════════════════════════════════════════════════════════════════════════

    /**
     * @brief Composites all layers and writes changes to stdout.
     *
     * Pipeline: clear → compositeScenes → strategy → syncPrevBuffers → flushOutput
     */
    public void render() {
        clear();
        compositeScenes();
        sb.setLength(0);

        if (dirtyRectanglesEnabled && diffRenderingEnabled && !forceFullRedraw) {
            if (renderDirtyRectangles()) {
                flushOutput();
            } else {
                lastFlushedBytes = 0;
            }
            return;
        }

        if (!diffRenderingEnabled || forceFullRedraw) {
            renderFullRedraw();
        } else {
            renderDiff();
        }

        syncPrevBuffers();
        flushOutput();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Rendering strategies
    // ════════════════════════════════════════════════════════════════════════

    /**
     * @brief Dirty-rectangles strategy: finds the bounding box of changed cells
     *        and rewrites only that region.
     * @return True if any dirty cells were found and written, false if nothing changed.
     */
    private boolean renderDirtyRectangles() {
        int minX = width, maxX = -1, minY = height, maxY = -1;
        final int cells = compositeCodepoints.length;

        for (int i = 0; i < cells; i++) {
            if (compositeCodepoints[i] != prevCodepoints[i]
                    | compositeFg[i] != prevFg[i]
                    | compositeBg[i] != prevBg[i]) {
                int y = i / width;
                int x = i - y * width;
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }
        }

        if (maxX == -1) return false;

        int curFg = -2, curBg = -2;
        for (int row = minY; row <= maxY; row++) {
            moveCursor(sb, row, minX);
            curFg = -2; curBg = -2;
            int base = row * width;
            for (int col = minX; col <= maxX; col++) {
                int i  = base + col;
                int cp = compositeCodepoints[i];
                if (cp == -99) continue;
                int fg = compositeFg[i];
                int bg = compositeBg[i];
                if (fg != curFg) { emitFg(sb, fg); curFg = fg; }
                if (bg != curBg) { emitBg(sb, bg); curBg = bg; }
                if (Character.isValidCodePoint(cp)) sb.appendCodePoint(cp); else sb.append(' ');
            }
        }
        sb.append(FastANSI.RESET);

        // Sync only the dirty region
        for (int row = minY; row <= maxY; row++) {
            int offset = row * width + minX;
            int len    = maxX - minX + 1;
            System.arraycopy(compositeCodepoints, offset, prevCodepoints, offset, len);
            System.arraycopy(compositeFg,         offset, prevFg,         offset, len);
            System.arraycopy(compositeBg,         offset, prevBg,         offset, len);
        }
        return true;
    }

    /**
     * @brief Full-redraw strategy: rewrites every cell from CURSOR_HOME using newlines.
     *        Used on first frame or when diff is disabled.
     */
    private void renderFullRedraw() {
        System.out.print(FastANSI.CURSOR_HOME);
        int curFg = -2, curBg = -2;
        final int cells = compositeCodepoints.length;

        for (int i = 0; i < cells; i++) {
            int cp = compositeCodepoints[i];
            if (cp != -99) {
                int fg = compositeFg[i];
                int bg = compositeBg[i];
                if (fg != curFg) { emitFg(sb, fg); curFg = fg; }
                if (bg != curBg) { emitBg(sb, bg); curBg = bg; }
                if (Character.isValidCodePoint(cp)) sb.appendCodePoint(cp); else sb.append(' ');
            }
            // Newline at end of each row (skip last row to avoid scroll)
            if ((i + 1) % width == 0 && (i + 1) < cells) {
                sb.append(FastANSI.RESET).append('\n');
                curFg = -2; curBg = -2;
            }
        }
        sb.append(FastANSI.RESET);
        forceFullRedraw = false;
    }

    /**
     * @brief Diff strategy: emits only cells that changed since the last frame.
     *        Uses gap-fill for small skips to avoid expensive cursor jumps.
     */
    private void renderDiff() {
        int curFg = -2, curBg = -2;
        int expectedPos = -1;
        final int cells = compositeCodepoints.length;

        for (int i = 0; i < cells; i++) {
            int cp = compositeCodepoints[i];
            int fg = compositeFg[i];
            int bg = compositeBg[i];

            if (cp == prevCodepoints[i] && fg == prevFg[i] && bg == prevBg[i]) continue;

            if (cp == -99) {
                expectedPos = ((i + 1) % width == 0) ? -1 : i + 1;
                continue;
            }

            if (i != expectedPos) {
                int gap = i - expectedPos;
                // Gap-fill: cheaper than a cursor jump for small same-row gaps
                if (expectedPos >= 0 && gap > 0 && gap <= 4
                        && (i / width) == ((expectedPos - 1) / width)) {
                    for (int g = expectedPos; g < i; g++) {
                        int gcp = compositeCodepoints[g];
                        if (gcp == -99) continue;
                        int gfg = compositeFg[g];
                        int gbg = compositeBg[g];
                        if (gfg != curFg) { emitFg(sb, gfg); curFg = gfg; }
                        if (gbg != curBg) { emitBg(sb, gbg); curBg = gbg; }
                        if (Character.isValidCodePoint(gcp)) sb.appendCodePoint(gcp); else sb.append(' ');
                    }
                } else {
                    moveCursor(sb, i / width, i % width);
                }
            }

            if (fg != curFg) { emitFg(sb, fg); curFg = fg; }
            if (bg != curBg) { emitBg(sb, bg); curBg = bg; }
            if (Character.isValidCodePoint(cp)) sb.appendCodePoint(cp); else sb.append(' ');

            expectedPos = ((i + 1) % width == 0) ? -1 : i + 1;
        }
        sb.append(FastANSI.RESET);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Pipeline helpers
    // ════════════════════════════════════════════════════════════════════════

    /** @brief Updates and composites all registered scene layers into the composite buffers. */
    private void compositeScenes() {
        for (final FastTerminalScene scene : scenes) {
            if (scene.isDirty()) {
                scene.update();
                scene.setDirty(false);
            }
            insertScene(scene);
        }
    }

    /** @brief Copies composite buffers into prev buffers to track the last flushed frame. */
    private void syncPrevBuffers() {
        System.arraycopy(compositeCodepoints, 0, prevCodepoints, 0, compositeCodepoints.length);
        System.arraycopy(compositeFg,         0, prevFg,         0, compositeFg.length);
        System.arraycopy(compositeBg,         0, prevBg,         0, compositeBg.length);
    }

    /** @brief Encodes the StringBuilder contents as UTF-8 and writes to stdout. */
    private void flushOutput() {
        if (sb.length() == 0) { lastFlushedBytes = 0; return; }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        lastFlushedBytes = bytes.length;
        System.out.write(bytes, 0, bytes.length);
        System.out.flush();
    }

    // ════════════════════════════════════════════════════════════════════════
    // ANSI emission helpers
    // ════════════════════════════════════════════════════════════════════════

    private static void emitFg(final StringBuilder sb, final int fg) {
        if (fg == -1) sb.append(FastANSI.FG_DEFAULT);
        else sb.append(RGB_FG_PREFIX)
               .append((fg >>> 16) & 0xFF).append(';')
               .append((fg >>> 8)  & 0xFF).append(';')
               .append( fg         & 0xFF).append(RGB_SUFFIX);
    }

    private static void emitBg(final StringBuilder sb, final int bg) {
        if (bg == -1) sb.append(FastANSI.BG_DEFAULT);
        else sb.append(RGB_BG_PREFIX)
               .append((bg >>> 16) & 0xFF).append(';')
               .append((bg >>> 8)  & 0xFF).append(';')
               .append( bg         & 0xFF).append(RGB_SUFFIX);
    }

    private static void moveCursor(final StringBuilder sb, final int row, final int col) {
        sb.append(FastANSI.CSI).append(row + 1).append(';').append(col + 1).append('H');
    }

    // ════════════════════════════════════════════════════════════════════════
    // Scene compositing
    // ════════════════════════════════════════════════════════════════════════

    /**
     * @brief Blits a scene layer into the composite buffers.
     *
     * Opaque scenes overwrite all cells. Transparent scenes skip blank cells
     * (space, fg=-1, bg=-1) so lower layers show through.
     */
    private void insertScene(final FastTerminalScene scene) {
        final int[] srcCp  = scene.getCodepointBuffer();
        final int[] srcFg  = scene.getFgBuffer();
        final int[] srcBg  = scene.getBgBuffer();
        final int srcWidth  = scene.getWidth();
        final int srcHeight = scene.getHeight();
        final int dstX      = scene.getX();
        final int dstY      = scene.getY();
        final boolean transparent = scene.isTransparentBackground();

        for (int row = 0; row < srcHeight; row++) {
            final int dstRow = dstY + row;
            if (dstRow < 0 || dstRow >= height) continue;

            int srcPos = row * srcWidth;
            int dstPos = dstRow * width + dstX;
            int length = srcWidth;

            // Clip left edge
            if (dstX < 0) {
                srcPos -= dstX;
                length += dstX;
                dstPos  = dstRow * width;
            }
            // Clip right edge
            if (dstX + length > width) length = width - dstX;
            if (length <= 0) continue;

            if (!transparent) {
                System.arraycopy(srcCp, srcPos, compositeCodepoints, dstPos, length);
                System.arraycopy(srcFg, srcPos, compositeFg,         dstPos, length);
                System.arraycopy(srcBg, srcPos, compositeBg,         dstPos, length);
            } else {
                for (int i = 0; i < length; i++) {
                    int scp = srcCp[srcPos + i];
                    int sfg = srcFg[srcPos + i];
                    int sbg = srcBg[srcPos + i];
                    if (scp == ' ' && sfg == -1 && sbg == -1) continue;
                    compositeCodepoints[dstPos + i] = scp;
                    compositeFg        [dstPos + i] = sfg;
                    compositeBg        [dstPos + i] = sbg;
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Buffer management
    // ════════════════════════════════════════════════════════════════════════

    /** @brief Resets composite buffers to blank/default. */
    public void clear() {
        Arrays.fill(compositeCodepoints, ' ');
        Arrays.fill(compositeFg,         -1);
        Arrays.fill(compositeBg,         -1);
    }

    /** @brief Resets prev buffers and forces a full redraw on the next render call. */
    public void clearPrev() {
        if (prevCodepoints != null) {
            Arrays.fill(prevCodepoints, ' ');
            Arrays.fill(prevFg,         -1);
            Arrays.fill(prevBg,         -1);
        }
        forceFullRedraw = true;
    }

    /**
     * @brief Suppresses the initial full-redraw pass.
     *
     * Useful for overlay scenarios: the prev-buffers start blank/default, matching
     * an empty canvas, so the diff engine only emits cells that differ — i.e. the
     * panel — leaving the existing terminal content untouched.
     */
    public void suppressInitialFullRedraw() {
        forceFullRedraw = false;
    }

    /**
     * @brief Resizes all buffers in-place.
     * @return True if dimensions actually changed.
     */
    public boolean resize(final int newWidth, final int newHeight) {
        if (newWidth <= 0 || newHeight <= 0)                       return false;
        if (newWidth == width && newHeight == height)              return false;

        width  = newWidth;
        height = newHeight;
        int cells = newWidth * newHeight;

        compositeCodepoints = new int[cells];
        compositeFg         = new int[cells];
        compositeBg         = new int[cells];
        prevCodepoints      = new int[cells];
        prevFg              = new int[cells];
        prevBg              = new int[cells];

        clear();
        clearPrev();
        return true;
    }

    /** @brief Releases all buffer and scene references. */
    public void dispose() {
        if (scenes != null) { scenes.clear(); scenes = null; }
        compositeCodepoints = null;
        compositeFg         = null;
        compositeBg         = null;
        prevCodepoints      = null;
        prevFg              = null;
        prevBg              = null;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Overlay restore (shutdown hook use)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * @brief Restores the composite buffer to stdout using absolute cursor positioning.
     *
     * Writes every cell of every row up to the last row with real content, using
     * ESC[row;1H jumps — no CURSOR_HOME, no newlines, no scroll risk.
     * Places the cursor at the row after last content so the shell prompt lands there.
     *
     * Safe to call from a shutdown hook.
     */
    public void renderAbsolute() {
        clear();
        compositeScenes();

        sb.setLength(0);

        // Find last row with any non-blank content (scan from bottom)
        int lastContentRow = 0;
        outer:
        for (int row = height - 1; row >= 0; row--) {
            int base = row * width;
            for (int col = 0; col < width; col++) {
                int i = base + col;
                if (!((compositeCodepoints[i] == ' ' || compositeCodepoints[i] == 0)
                        && compositeFg[i] == -1 && compositeBg[i] == -1)) {
                    lastContentRow = row;
                    break outer;
                }
            }
        }

        int curFg = -2, curBg = -2;
        for (int row = 0; row <= lastContentRow; row++) {
            moveCursor(sb, row, 0);
            curFg = -2; curBg = -2;
            int base = row * width;
            for (int col = 0; col < width; col++) {
                int i  = base + col;
                int cp = compositeCodepoints[i];
                int fg = compositeFg[i];
                int bg = compositeBg[i];
                if (cp == -99) { sb.append(' '); continue; }
                if (fg != curFg) { emitFg(sb, fg); curFg = fg; }
                if (bg != curBg) { emitBg(sb, bg); curBg = bg; }
                if (Character.isValidCodePoint(cp)) sb.appendCodePoint(cp); else sb.append(' ');
            }
        }

        int promptRow = Math.min(lastContentRow + 2, height);
        sb.append(FastANSI.RESET);
        moveCursor(sb, promptRow - 1, 0); // moveCursor is 0-based internally, adds +1

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        System.out.write(bytes, 0, bytes.length);
        System.out.flush();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Accessors
    // ════════════════════════════════════════════════════════════════════════

    public boolean isDiffRenderingEnabled()    { return diffRenderingEnabled; }
    public boolean isDirtyRectanglesEnabled()  { return dirtyRectanglesEnabled; }
    public int     getLastFlushedBytes()       { return lastFlushedBytes; }
    public int     getWidth()                  { return width; }
    public int     getHeight()                 { return height; }

    public void setDiffRenderingEnabled(boolean enabled) {
        diffRenderingEnabled = enabled;
        if (!enabled) forceFullRedraw = true;
    }

    public void setDirtyRectanglesEnabled(boolean enabled) {
        dirtyRectanglesEnabled = enabled;
        if (enabled) clearPrev();
    }
}
