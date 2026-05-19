package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;
import java.util.Random;

/**
 * 🌌 Infinite 3D Starfield Tunnel Warp effect.
 * Projects a high-speed hyperdrive tunnel of rotating stars scaling outwards
 * through a curving cylindrical vector pipeline.
 */
public class WarpTunnelEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double phase = 0.0;

    private static final int STAR_COUNT = 150;
    private double[] starX = new double[STAR_COUNT];
    private double[] starY = new double[STAR_COUNT];
    private double[] starZ = new double[STAR_COUNT];

    private Random rand = new Random();

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        // Populate 3D space coordinates
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = (rand.nextDouble() - 0.5) * 40.0;
            starY[i] = (rand.nextDouble() - 0.5) * 20.0;
            starZ[i] = 0.1 + rand.nextDouble() * 20.0; // Distance
        }
    }

    @Override
    public void update(long frameIndex) {
        phase = frameIndex * 0.02;

        // Move stars closer to camera (decrease Z)
        for (int i = 0; i < STAR_COUNT; i++) {
            starZ[i] -= 0.15; // Speed

            // If star passes camera, recycle it to the far end
            if (starZ[i] <= 0.1) {
                starX[i] = (rand.nextDouble() - 0.5) * 40.0;
                starY[i] = (rand.nextDouble() - 0.5) * 20.0;
                starZ[i] = 20.0;
            }
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // Velvet space backdrop
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                canvas.writeCell(x, y, ' ', 0, 0x010105);
            }
        }

        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double aspect = 2.0;

        // Dynamically compute curving tunnel center
        double tunnelCenterX = centerX + 10.0 * Math.sin(phase * 1.5);
        double tunnelCenterY = centerY + 5.0 * Math.cos(phase * 0.8);

        // Project and draw stars
        for (int i = 0; i < STAR_COUNT; i++) {
            double z = starZ[i];
            double scale = (height * 0.6) / z;

            // Apply spiral angle rotation based on depth Z
            double angleOffset = phase + z * 0.12;
            double cos = Math.cos(angleOffset);
            double sin = Math.sin(angleOffset);

            double rx = starX[i] * cos - starY[i] * sin;
            double ry = starX[i] * sin + starY[i] * cos;

            int px = (int) (tunnelCenterX + rx * scale * aspect);
            int py = (int) (tunnelCenterY + ry * scale);

            if (px >= 0 && px < width && py >= 0 && py < height) {
                // Closer = brighter cyan/white, further = deep purple
                double depth = 1.0 - (z / 20.0);
                if (depth < 0) depth = 0;

                int color;
                char glyph;
                if (depth > 0.8) {
                    color = 0xFFFFFF; // White spark
                    glyph = '█';
                } else if (depth > 0.5) {
                    color = 0x22D3EE; // Cyber Cyan
                    glyph = '▓';
                } else {
                    color = 0x8B5CF6; // Violet
                    glyph = '░';
                }

                canvas.writeCell(px, py, glyph, color, 0x010105);
            }
        }
    }

    @Override
    public String getName() {
        return "🌀 Infinite 3D Starfield Tunnel Warp";
    }
}
