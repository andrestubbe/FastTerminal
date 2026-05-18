package fastterminal;

import java.util.Arrays;

/**
 * Represents a high-performance grid viewport scene.
 * Utilizes primitive arrays (int[]) for UTF-32 Codepoints, Foreground, and Background True Colors.
 */
public class FastTerminalScene {

    private int[] codepointBuffer;
    private int[] fgBuffer;
    private int[] bgBuffer;

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private Runnable updater;
    private boolean dirty;

    /**
     * Creates a new terminal scene viewport.
     * @param x viewport X coordinate (0-indexed)
     * @param y viewport Y coordinate (0-indexed)
     * @param width viewport width
     * @param height viewport height
     */
    public FastTerminalScene(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.codepointBuffer = new int[width * height];
        this.fgBuffer = new int[width * height];
        this.bgBuffer = new int[width * height];
        this.dirty = true;
        this.clear();
    }

    public void update() {
        if (this.updater != null) {
            this.clear();
            this.updater.run();
        }
    }

    /**
     * Clears the scene buffers, setting defaults:
     * - Codepoint: Space (' ')
     * - Foreground: Default (-1)
     * - Background: Default (-1)
     */
    public void clear() {
        Arrays.fill(this.codepointBuffer, ' ');
        Arrays.fill(this.fgBuffer, -1);
        Arrays.fill(this.bgBuffer, -1);
    }

    /**
     * Writes a UTF-32 Codepoint to a specific cell in the scene.
     */
    public void writeCell(int col, int row, int codepoint, int fg, int bg) {
        if (col >= 0 && col < this.width && row >= 0 && row < this.height) {
            int idx = row * this.width + col;
            this.codepointBuffer[idx] = codepoint;
            this.fgBuffer[idx] = fg;
            this.bgBuffer[idx] = bg;
        }
    }

    /**
     * Writes a string safely to the viewport, supporting emojis and 24-bit True Colors.
     */
    public void writeString(int startCol, int row, String text, int fg, int bg) {
        if (row < 0 || row >= this.height) return;
        int len = text.length();
        int col = startCol;
        for (int i = 0; i < len; ) {
            if (col >= this.width) break;
            int cp = text.codePointAt(i);
            this.writeCell(col, row, cp, fg, bg);
            col++;
            i += Character.charCount(cp);
        }
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void setDirty(final boolean dirty) {
        this.dirty = dirty;
    }

    public void setUpdater(final Runnable updater) {
        this.updater = updater;
    }

    public int[] getCodepointBuffer() {
        return this.codepointBuffer;
    }

    public int[] getFgBuffer() {
        return this.fgBuffer;
    }

    public int[] getBgBuffer() {
        return this.bgBuffer;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void dispose() {
        this.codepointBuffer = null;
        this.fgBuffer = null;
        this.bgBuffer = null;
        this.updater = null;
    }
}
