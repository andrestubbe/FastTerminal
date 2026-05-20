package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;
import java.util.Random;

/**
 * @class MatrixRainEffect
 * @brief 💊 High-performance Matrix Rain falling digital stream visual effect.
 * 
 * Simulates bright green vertical digital drops with glowing white lead glyphs,
 * dynamic tail decays, and changing katakana/alphanumeric characters.
 */
public class MatrixRainEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double time = 0.0;
    
    private double[] heads;
    private double[] speeds;
    private int[] tailLengths;
    private Random random = new Random();

    // Half-width Japanese katakana & alphanumeric runes (EAW width 1)
    private static final String MATRIX_CHARS = "ｦｧｨｩｪｫｬｭｮｯｰｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ1234567890*+-<>[]";

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
        this.heads = new double[width];
        this.speeds = new double[width];
        this.tailLengths = new int[width];

        for (int i = 0; i < width; i++) {
            resetColumn(i, true);
        }
    }

    private void resetColumn(int col, boolean randomizeStart) {
        if (randomizeStart) {
            // Distribute column heads throughout the screen height to start populated
            heads[col] = random.nextInt(height + 40) - 20;
        } else {
            // Sparse entry: start columns at different negative offsets to space them out
            heads[col] = -random.nextInt(height * 2) - 10;
        }
        speeds[col] = 4.0 + random.nextDouble() * 16.0;  // 4 to 20 cells per second
        tailLengths[col] = 8 + random.nextInt(16);       // 8 to 24 cells tail length
    }

    @Override
    public void update(double time, double deltaTime) {
        this.time = time;
        
        for (int i = 0; i < width; i++) {
            heads[i] += speeds[i] * deltaTime;
            // If the tail has fully cleared the bottom of the screen, reset
            if (heads[i] - tailLengths[i] > height) {
                resetColumn(i, false);
            }
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        // 1. Clear background to absolute solid black
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                canvas.writeCell(x, y, ' ', -1, 0x000000);
            }
        }

        // 2. Draw falling digital rain columns
        for (int x = 0; x < width; x++) {
            double headY = heads[x];
            int tailLen = tailLengths[x];

            for (int y = 0; y < height; y++) {
                // If this cell is within the active stream boundaries
                if (y <= headY && y >= headY - tailLen) {
                    double age = (headY - y) / tailLen; // 0.0 at head, 1.0 at tail end
                    
                    int fgColor;
                    int bgColor = 0x000000;

                    if (age < 0.05) {
                        // Glowing leading head character (Crisp White with a dark green backing glow)
                        fgColor = 0xFFFFFF;
                        bgColor = 0x003300;
                    } else if (age < 0.20) {
                        // High-intensity lime neon green right behind the head
                        fgColor = 0x39FF14;
                    } else {
                        // Fading shades of matrix green down the tail
                        int gVal = (int) (220 * (1.0 - age) + 35);
                        if (gVal > 255) gVal = 255;
                        if (gVal < 0) gVal = 0;
                        fgColor = (0 << 16) | (gVal << 8) | 0;
                    }

                    // Dynamically choose and change character over time (based on time and column speed)
                    int charSeed = x * 73 + y * 31 + (int) (time * speeds[x] * 0.35);
                    char glyph = MATRIX_CHARS.charAt(Math.abs(charSeed) % MATRIX_CHARS.length());

                    canvas.writeCell(x, y, glyph, fgColor, bgColor);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Matrix Rain";
    }
}
