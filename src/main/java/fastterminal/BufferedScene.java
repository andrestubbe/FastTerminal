package fastterminal;

import java.util.Arrays;

public class BufferedScene implements fastansi.CellConsumer {

    private final int width;
    private final int height;

    private final int[] codepoints;
    private final long[] colors; // [32-bit fg | 32-bit bg], 0 = transparent

    public BufferedScene(int width, int height) {
        this.width = width;
        this.height = height;
        this.codepoints = new int[width * height];
        this.colors = new long[width * height];
        clear();
    }

    public void clear() {
        Arrays.fill(codepoints, ' ');
        Arrays.fill(colors, 0L); // 0 = komplett transparent
    }

    @Override
    public void writeCell(int col, int row, int codepoint, int fgColor, int bgColor) {
        if ((col | row) >= 0 && col < width && row < height) {
            int idx = row * width + col;

            long current = colors[idx];
            int oldFg = (int) (current >>> 32);
            int oldBg = (int) current;

            // If current is 0L (fully transparent), treat defaults as -2 (transparent)
            if (current == 0L) {
                oldFg = -2;
                oldBg = -2;
            }

            int newFg = fgColor != -2 ? fgColor : oldFg;
            int newBg = bgColor != -2 ? bgColor : oldBg;

            if (newFg == -2 && newBg == -2) {
                colors[idx] = 0L; // completely transparent
            } else {
                colors[idx] = ((long)newFg << 32) | (newBg & 0xFFFFFFFFL);
            }

            if (codepoint != -2) {
                codepoints[idx] = codepoint;
            }
        }
    }

    public void drawTo(FastTerminalScene dest, int destX, int destY) {

        int destW = dest.getWidth();
        int destH = dest.getHeight();

        int startRow = destY < 0 ? -destY : 0;
        int endRow   = destY + height > destH ? destH - destY : height;
        if (startRow >= endRow) return;

        int startCol = destX < 0 ? -destX : 0;
        int endCol   = destX + width > destW ? destW - destX : width;
        if (startCol >= endCol) return;

        int[] dCode = dest.getCodepointBuffer();
        int[] dFg   = dest.getFgBuffer();
        int[] dBg   = dest.getBgBuffer();

        int[] sCode = this.codepoints;
        long[] sColors = this.colors;

        int clippedWidth = endCol - startCol;

        for (int r = startRow; r < endRow; r++) {

            int srcIdx  = r * width + startCol;
            int destIdx = (destY + r) * destW + (destX + startCol);

            for (int i = 0; i < clippedWidth; i++) {

                long colorVal = sColors[srcIdx];

                if (colorVal != 0L) { // komplett transparent überspringen

                    int fgVal = (int)(colorVal >>> 32);
                    int bgVal = (int)colorVal;

                    if (fgVal != -2) {
                        dCode[destIdx] = sCode[srcIdx];
                        dFg[destIdx] = fgVal;
                    }
                    if (bgVal != -2) {
                        dBg[destIdx] = bgVal;
                    }
                }

                srcIdx++;
                destIdx++;
            }
        }

        dest.setDirty(true);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int[] getCodepoints() { return codepoints; }
    public long[] getColors() { return colors; }
}
