package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * High-performance digital Matrix Rain dropping columns with phosphorescent tail decay.
 */
public class MatrixRainEffect implements DemosceneEffect {

    private int width;
    private int height;
    private int[] dropY;
    private int[] dropSpeed;
    private int[] cellIntensity;
    private int[] cellChars;

    // Authentic Matrix katakana-style unicode character set bounds
    private static final String GLYPHS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZﾊﾐﾋｰｳｼﾅﾓﾆｻﾜﾂｵﾘｱﾎﾏｹﾒｴｶｷﾑﾕﾗｾﾈｽﾀﾇﾍｦｲｸｺｿﾁﾄﾉﾌﾔﾖﾙﾚ";

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
        this.dropY = new int[width];
        this.dropSpeed = new int[width];
        this.cellIntensity = new int[width * height];
        this.cellChars = new int[width * height];

        for (int x = 0; x < width; x++) {
            resetColumn(x);
            // Stagger initial entry
            dropY[x] = -(int) (Math.random() * height);
        }
    }

    private void resetColumn(int x) {
        dropY[x] = -1;
        dropSpeed[x] = (int) (Math.random() * 2) + 1;
    }

    @Override
    public void update(long frameIndex) {
        // 1. Decay overall phosphorescent cell intensities
        for (int i = 0; i < cellIntensity.length; i++) {
            if (cellIntensity[i] > 0) {
                cellIntensity[i] = Math.max(0, cellIntensity[i] - 12); // Speed of trail fading
            }
        }

        // 2. Advance falling drop heads per column
        for (int x = 0; x < width; x++) {
            // Speed control gate
            if (frameIndex % dropSpeed[x] == 0) {
                dropY[x]++;

                if (dropY[x] >= height) {
                    resetColumn(x);
                } else if (dropY[x] >= 0) {
                    int pos = x + dropY[x] * width;
                    cellIntensity[pos] = 255; // White hot drop leading edge
                    // Select a random Matrix rain character
                    cellChars[pos] = GLYPHS.charAt((int) (Math.random() * GLYPHS.length()));
                }
            }

            // Occasional character flicker for digital organic look
            if (Math.random() < 0.05) {
                int ry = (int) (Math.random() * height);
                int pos = x + ry * width;
                if (cellIntensity[pos] > 50) {
                    cellChars[pos] = GLYPHS.charAt((int) (Math.random() * GLYPHS.length()));
                }
            }
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pos = x + y * width;
                int intensity = cellIntensity[pos];

                if (intensity <= 0) {
                    canvas.writeCell(x, y, ' ', 0x000000, 0x000000);
                } else {
                    int charCode = cellChars[pos];
                    int fgColor;

                    if (intensity >= 240) {
                        // Glowing leading head is bright neon white
                        fgColor = 0xE0FFE0;
                    } else {
                        // Tail decays to deep phosphorescent Matrix green
                        int green = (intensity * 180) / 255 + 40;
                        int red = (intensity * 20) / 255;
                        int blue = (intensity * 20) / 255;
                        fgColor = (red << 16) | (green << 8) | blue;
                    }

                    canvas.writeCell(x, y, charCode, fgColor, 0x000000);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "💊 MATRIX RAIN GLYPHS";
    }
}
