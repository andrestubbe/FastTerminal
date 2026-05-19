package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

import java.util.Arrays;

/**
 * Authentic 90s Doom Fire effect optimized for FastTerminal True-Color.
 */
public class DoomFireEffect implements DemosceneEffect {

    private int width;
    private int height;
    private int[] firePixels;

    // Authentic 37-color palette from black through dark reds, oranges, yellows, and white
    private static final int[] PALETTE = {
        0x070707, 0x1F0707, 0x2F0F07, 0x470F07, 0x571707, 0x671F07, 0x771F07, 0x8F2707,
        0x9F2F07, 0xAF3F07, 0xBF4707, 0xC74707, 0xDF4F07, 0xDF5707, 0xDF570F, 0xD75F0F,
        0xD7670F, 0xCF6F0F, 0xCF770F, 0xCF7F0F, 0xCF8717, 0xC78717, 0xC78F17, 0xC7971F,
        0xBF9F1F, 0xBF9F27, 0xBFA727, 0xBFA72F, 0xBFAF2F, 0xBFAF37, 0xB7B737, 0xB7B73F,
        0xB7BF47, 0xB7BF4F, 0xC7C76F, 0xDFDF9F, 0xFFFFC7, 0xFFFFFF
    };

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
        this.firePixels = new int[width * height];
        
        // Fill base row with maximum heat (white hot)
        int lastRowStart = (height - 1) * width;
        for (int i = 0; i < width; i++) {
            firePixels[lastRowStart + i] = PALETTE.length - 1;
        }
    }

    @Override
    public void update(long frameIndex) {
        // Propagate heat upwards from row 1 down to the bottom
        for (int x = 0; x < width; x++) {
            for (int y = 1; y < height; y++) {
                int src = x + y * width;
                int pixel = firePixels[src];

                if (pixel == 0) {
                    firePixels[src - width] = 0;
                } else {
                    // Decay heat randomly
                    int decay = (int) (Math.random() * 2.0);
                    // Shifting wind effect leftwards
                    int windShift = (int) (Math.random() * 3.0);
                    int dest = src - width - windShift + 1;

                    if (dest >= 0 && dest < firePixels.length) {
                        int newVal = pixel - decay;
                        firePixels[dest] = newVal < 0 ? 0 : newVal;
                    }
                }
            }
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int val = firePixels[x + y * width];
                int color = PALETTE[val];
                // Draw fire particles using high-density blocks
                canvas.writeCell(x, y, '█', color, 0x070707);
            }
        }
    }

    @Override
    public String getName() {
        return "🔥 DOOM FIRE EFFECT";
    }
}
