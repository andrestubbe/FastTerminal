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

    private int x;
    private int y;
    private int width;
    private int height;
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
     * Resizes the scene viewport dimensions dynamically in-place.
     * @return true if a resize actually occurred, false otherwise.
     */
    public boolean resize(final int newWidth, final int newHeight) {
        if (newWidth <= 0 || newHeight <= 0) return false;
        if (newWidth == this.width && newHeight == this.height) return false;

        this.width = newWidth;
        this.height = newHeight;
        this.codepointBuffer = new int[newWidth * newHeight];
        this.fgBuffer = new int[newWidth * newHeight];
        this.bgBuffer = new int[newWidth * newHeight];
        this.clear();
        this.dirty = true;
        return true;
    }

    /**
     * Updates the viewport absolute screen offsets dynamically.
     */
    public void setPosition(final int newX, final int newY) {
        this.x = newX;
        this.y = newY;
        this.dirty = true;
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
            int width = fastemojis.FastEmojis.getWidth(cp);
            
            this.writeCell(col, row, cp, fg, bg);
            if (width == 2 && col + 1 < this.width) {
                this.writeCell(col + 1, row, -99, fg, bg); // Native continuation cell marker
            }
            
            col += width;
            i += Character.charCount(cp);
        }
    }

    private static final int[] ANSI_COLORS_24BIT = {
        0x000000, // 0: Black
        0xCD3131, // 1: Red
        0x0DBC79, // 2: Green
        0xE5E510, // 3: Yellow
        0x2472C8, // 4: Blue
        0xBC3FBC, // 5: Magenta
        0x11A8CD, // 6: Cyan
        0xE5E5E5, // 7: White
        0x666666, // 8: Bright Black (Gray)
        0xF14C4C, // 9: Bright Red
        0x23D18B, // 10: Bright Green
        0xF5F543, // 11: Bright Yellow
        0x3B8EEA, // 12: Bright Blue
        0xD670D6, // 13: Bright Magenta
        0x29B8DB, // 14: Bright Cyan
        0xFFFFFF  // 15: Bright White
    };

    private static int toTrueColor(int colorType, int r, int g, int b, int defaultColor) {
        if (colorType == 0) { // 4-bit
            if (r == -1) return defaultColor;
            if (r >= 0 && r < 16) return ANSI_COLORS_24BIT[r];
            return defaultColor;
        } else if (colorType == 1) { // 8-bit
            if (r >= 0 && r < 16) return ANSI_COLORS_24BIT[r];
            if (r >= 16 && r <= 231) {
                int idx = r - 16;
                int red = (idx / 36) * 51;
                int green = ((idx % 36) / 6) * 51;
                int blue = (idx % 6) * 51;
                return (red << 16) | (green << 8) | blue;
            }
            if (r >= 232 && r <= 255) {
                int gray = 8 + (r - 232) * 10;
                return (gray << 16) | (gray << 8) | gray;
            }
            return defaultColor;
        } else if (colorType == 2) { // 24-bit True Color RGB
            return (r << 16) | (g << 8) | b;
        }
        return defaultColor;
    }

    /**
     * Writes an ANSI escape-sequenced string dynamically to the viewport, 
     * parsing inline styles and True Colors on-the-fly with exactly zero allocations.
     */
    public void writeAnsiString(int startCol, int row, String text, int defaultFg, int defaultBg) {
        if (row < 0 || row >= this.height) return;
        
        final int[] col = { startCol };
        final int[] activeFg = { defaultFg };
        final int[] activeBg = { defaultBg };

        fastansi.FastANSI.parse(text, new fastansi.FastANSI.ANSIListener() {
            @Override
            public void onText(CharSequence text, int start, int end) {
                for (int i = start; i < end; ) {
                    if (col[0] >= width) break;
                    int cp = Character.codePointAt(text, i);
                    int charWidth = fastemojis.FastEmojis.getWidth(cp);
                    
                    writeCell(col[0], row, cp, activeFg[0], activeBg[0]);
                    if (charWidth == 2 && col[0] + 1 < width) {
                        writeCell(col[0] + 1, row, -99, activeFg[0], activeBg[0]);
                    }
                    
                    col[0] += charWidth;
                    i += Character.charCount(cp);
                }
            }

            @Override
            public void onReset() {
                activeFg[0] = defaultFg;
                activeBg[0] = defaultBg;
            }

            @Override
            public void onBold(boolean enable) {}
            @Override
            public void onItalic(boolean enable) {}
            @Override
            public void onUnderline(boolean enable) {}
            @Override
            public void onBlink(boolean enable) {}
            @Override
            public void onInvert(boolean enable) {}
            @Override
            public void onHide(boolean enable) {}
            @Override
            public void onStrikethrough(boolean enable) {}

            @Override
            public void onForegroundColor(int colorType, int r, int g, int b) {
                activeFg[0] = toTrueColor(colorType, r, g, b, defaultFg);
            }

            @Override
            public void onBackgroundColor(int colorType, int r, int g, int b) {
                activeBg[0] = toTrueColor(colorType, r, g, b, defaultBg);
            }

            @Override
            public void onCursorPosition(int row, int col) {}
            @Override
            public void onCursorUp(int count) {}
            @Override
            public void onCursorDown(int count) {}
            @Override
            public void onCursorForward(int count) {}
            @Override
            public void onCursorBackward(int count) {}
            @Override
            public void onCursorNextLine(int count) {}
            @Override
            public void onCursorPrevLine(int count) {}
            @Override
            public void onCursorHorizontalAbsolute(int col) {}
            @Override
            public void onEraseInDisplay(int mode) {}
            @Override
            public void onEraseInLine(int mode) {}
            @Override
            public void onScrollUp(int count) {}
            @Override
            public void onScrollDown(int count) {}
            @Override
            public void onPrivateMode(int mode, boolean enable) {}
            @Override
            public void onDeviceStatusReport() {}
            @Override
            public void onWindowTitle(CharSequence title, int start, int end) {}
            @Override
            public void onUnsupportedSequence(CharSequence raw, int start, int end) {}
        });
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
