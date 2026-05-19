package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

import java.util.Arrays;

/**
 * @class DoomFireEffect
 * @brief Authentic 90s classic Doom Fire particle propagation effect.
 * 
 * Simulates a vertical cellular heat grid propagating upwards from the bottom row,
 * combining custom randomized cooling decays and dynamic horizontal wind shifts.
 * Renders in true-color double resolution vertical coordinates mapped to terminal half-blocks (▄).
 */
public class DoomFireEffect implements DemosceneEffect {

    private int width;
    private int height;
    private int[] firePixels;
    private double timeAccumulator = 0.0;

    // Authentic 37-color palette from black through dark reds, oranges, yellows, and white
    private static final int[] PALETTE = {
        0x070707, 0x1F0707, 0x2F0F07, 0x470F07, 0x571707, 0x671F07, 0x771F07, 0x8F2707,
        0x9F2F07, 0xAF3F07, 0xBF4707, 0xC74707, 0xDF4F07, 0xDF5707, 0xDF570F, 0xD75F0F,
        0xD7670F, 0xCF6F0F, 0xCF770F, 0xCF7F0F, 0xCF8717, 0xC78717, 0xC78F17, 0xC7971F,
        0xBF9F1F, 0xBF9F27, 0xBFA727, 0xBFA72F, 0xBFAF2F, 0xBFAF37, 0xB7B737, 0xB7B73F,
        0xB7BF47, 0xB7BF4F, 0xC7C76F, 0xDFDF9F, 0xFFFFC7, 0xFFFFFF
    };

    /**
     * @brief Initializes the fire pixel buffer.
     * 
     * Sets the bottom-most subpixel row to maximum heat (white hot).
     * 
     * @param width Terminal screen width.
     * @param height Terminal screen height.
     */
    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
        // In half-block double resolution, the fire simulation is run on a 2 * height grid!
        this.firePixels = new int[width * (2 * height)];
        this.timeAccumulator = 0.0;
        
        // Fill base row with maximum heat (white hot)
        int lastRowStart = (2 * height - 1) * width;
        for (int i = 0; i < width; i++) {
            firePixels[lastRowStart + i] = PALETTE.length - 1;
        }
    }

    /**
     * @brief Propagates heat index upward and applies random wind shift and decay.
     * 
     * @param time Total elapsed time in seconds.
     * @param deltaTime Elapsed time in seconds since last frame.
     */
    @Override
    public void update(double time, double deltaTime) {
        timeAccumulator += deltaTime;
        double targetStep = 1.0 / 120.0; // Run fire updates at stable ~120 Hz
        while (timeAccumulator >= targetStep) {
            runFireStep();
            timeAccumulator -= targetStep;
        }
    }

    private void runFireStep() {
        // Propagate heat upwards from row 1 down to the bottom on the double-res grid
        for (int x = 0; x < width; x++) {
            for (int y = 1; y < 2 * height; y++) {
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

    /**
     * @brief Blits fire pixels onto the FastTerminalScene double-buffer screen.
     * 
     * Mappings:
     * - Top pixel -> cell background color.
     * - Bottom pixel -> cell foreground color.
     * - Character character -> '▄'.
     * 
     * @param canvas Double-buffer render target.
     */
    @Override
    public void render(FastTerminalScene canvas) {
        for (int row = 0; row < height; row++) {
            int yTop = 2 * row;
            int yBot = 2 * row + 1;

            for (int x = 0; x < width; x++) {
                int valTop = firePixels[x + yTop * width];
                int valBot = firePixels[x + yBot * width];

                int colorTop = PALETTE[valTop];
                int colorBot = PALETTE[valBot];

                // Write half-block cell: Foreground represents bottom pixel, Background represents top pixel
                canvas.writeCell(x, row, '▄', colorBot, colorTop);
            }
        }
    }

    /**
     * @brief Returns the visual user-friendly name of the effect.
     * @return String effect name label.
     */
    @Override
    public String getName() {
        return "Doom Fire Effect";
    }
}
