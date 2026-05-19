package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🟢 Cybernetic Digital Matrix Tunnel effect.
 * Projects coordinate plane into a 3D tunnel, rendering glowing green binary
 * and matrix runes falling along the cylindrical walls.
 */
public class AsciiTunnelEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double time = 0.0;

    private static final String RUNES = "0101XYZ#@$&*%";

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        time = frameIndex * 0.04;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double aspect = 2.0;

        for (int y = 0; y < height; y++) {
            double dy = (y - centerY) * aspect;

            for (int x = 0; x < width; x++) {
                double dx = x - centerX;

                double r = Math.sqrt(dx * dx + dy * dy);
                if (r < 1.0) r = 1.0; // Prevent divide by zero

                // 3D Tunnel cylindrical coordinates mapping: depth z = 1/r, angle = atan2
                double z = 25.0 / r;
                double angle = Math.atan2(dy, dx);

                // Discretize values to create character cells matching Matrix runes
                double u = (angle + Math.PI) / (2.0 * Math.PI) * 40.0;
                double v = z + time * 3.0;

                int iu = (int) Math.round(u);
                int iv = (int) Math.round(v);

                // Pseudo-random noise function based on u and v coordinates
                int hash = (iu * 17 + iv * 37) % 100;

                if (hash < 35) { // Stream distribution
                    // Falloff intensity from edge (brightest) to tunnel center (vanishing dark)
                    double intensity = Math.min(1.0, r / (width * 0.45));
                    if (intensity < 0.05) intensity = 0.05;

                    // Neon Green matrix shading
                    int rVal = (int) (16 * intensity);
                    int gVal = (int) (185 * intensity);
                    int bVal = (int) (129 * intensity);
                    int color = (rVal << 16) | (gVal << 8) | bVal;

                    // Fetch random character rune
                    char glyph = RUNES.charAt(Math.abs(iu + iv) % RUNES.length());
                    canvas.writeCell(x, y, glyph, color, 0x010502);
                } else {
                    canvas.writeCell(x, y, ' ', 0, 0x000000);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🟢 Cybernetic Digital Matrix Tunnel";
    }
}
