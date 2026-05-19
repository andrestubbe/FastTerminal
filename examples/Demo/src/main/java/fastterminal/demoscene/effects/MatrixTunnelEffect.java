package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🌀 3D Matrix Digital Rain Tunnel flight effect.
 * Projects infinite curving tunnel coordinates in 3D perspective,
 * cascading glowing green binary trails along the spiraling cylindrical tube.
 */
public class MatrixTunnelEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double time = 0.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        time = frameIndex * 0.08;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double aspect = 2.0;

        // Curving tunnel center coordinates offset dynamically
        double tunnelCenterX = centerX + 12.0 * Math.sin(time * 0.4);
        double tunnelCenterY = centerY + 6.0 * Math.cos(time * 0.3);

        for (int y = 0; y < height; y++) {
            double dy = (y - tunnelCenterY) * aspect;

            for (int x = 0; x < width; x++) {
                double dx = x - tunnelCenterX;

                double r = Math.sqrt(dx * dx + dy * dy);
                if (r < 1.0) r = 1.0;

                // Cylindrical perspective mapping
                double z = 22.0 / r;
                double angle = Math.atan2(dy, dx);

                // Grid mapping
                int u = (int) Math.round((angle + Math.PI) / (2.0 * Math.PI) * 48.0);
                int v = (int) Math.round(z + time * 3.5);

                // Binary rain streams distribution
                int hash = (u * 31 + v * 17) % 100;

                if (hash < 32) {
                    double intensity = r / (width * 0.42);
                    if (intensity > 1.0) intensity = 1.0;
                    if (intensity < 0.05) intensity = 0.05;

                    // Neon Green matrix colors
                    int rVal = (int) (16 * intensity);
                    int gVal = (int) (185 * intensity);
                    int bVal = (int) (129 * intensity);
                    int color = (rVal << 16) | (gVal << 8) | bVal;

                    char glyph = ((u + v) % 2 == 0) ? '1' : '0';
                    canvas.writeCell(x, y, glyph, color, 0x010402);
                } else {
                    canvas.writeCell(x, y, ' ', 0, 0x000000);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🌀 3D Matrix Digital Rain Tunnel";
    }
}
