package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * @class AsciiTunnelEffect
 * @brief 🟢 Cybernetic Digital Matrix Tunnel visual effect.
 * 
 * Maps flat 2D viewport coordinates directly to 3D polar cylinder coordinates (depth z = 1/r, angle = atan2).
 * Simulates falling matrix streams down a vanishing tunnel utilizing custom noise hashes and green-ambient depth falloffs.
 */
public class AsciiTunnelEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double time = 0.0;

    private static final String RUNES = "█▓▒░";

    /**
     * @brief Initializes view size coordinates.
     * @param width Terminal screen width.
     * @param height Terminal screen height.
     */
    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * @brief Advances tunnel scroll velocity.
     * @param time Total elapsed time in seconds.
     * @param deltaTime Elapsed time in seconds since last frame.
     */
    @Override
    public void update(double time, double deltaTime) {
        this.time = time * 4.8;
    }

    /**
     * @brief Transforms view coordinates and flushes matrix runes to the viewport canvas.
     * @param canvas Double-buffer render target.
     */
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

    /**
     * @brief Returns the visual user-friendly name of the effect.
     * @return String effect name label.
     */
    @Override
    public String getName() {
        return "Cybernetic Digital Matrix Tunnel";
    }
}
