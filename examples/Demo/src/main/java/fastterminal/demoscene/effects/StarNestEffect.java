package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🌌 Star Nest / Infinite Deep Space Nebula Fractal effect.
 * Synthesizes dynamic glowing stars, cosmic dust, and overlapping gas clouds
 * using multi-layered space projection and harmonic trig calculations.
 */
public class StarNestEffect implements DemosceneEffect {

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
        time = frameIndex * 0.015;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        double aspect = 2.0;

        for (int y = 0; y < height; y++) {
            double dy = (y - height / 2.0) * aspect / height;

            for (int x = 0; x < width; x++) {
                double dx = (x - width / 2.0) / (double) width;

                // Projection setup
                double r = Math.sqrt(dx * dx + dy * dy);
                double theta = Math.atan2(dy, dx);

                // Multi-frequency layers mimicking fractal zoom depth
                double value = 0.0;
                for (int layer = 1; layer <= 4; layer++) {
                    double scale = layer * 4.0;
                    value += (1.0 / layer) * Math.sin(r * scale - time * layer + Math.cos(theta * 3.0 + time));
                }

                // Smooth mapping
                double intensity = 0.5 + 0.5 * Math.sin(value * 2.0);

                if (intensity > 0.8) {
                    // Nebulous super-clusters (Bright Magenta / Soft Pink)
                    int rColor = (int) (236 * intensity);
                    int gColor = (int) (72 * intensity);
                    int bColor = (int) (153 * intensity);
                    int col = (rColor << 16) | (gColor << 8) | bColor;
                    canvas.writeCell(x, y, '█', col, 0x010005);
                } else if (intensity > 0.55) {
                    // Star cluster cores (Deep Cyan / Teal)
                    int rColor = (int) (6 * intensity);
                    int gColor = (int) (182 * intensity);
                    int bColor = (int) (212 * intensity);
                    int col = (rColor << 16) | (gColor << 8) | bColor;
                    canvas.writeCell(x, y, '▓', col, 0x000105);
                } else if (intensity > 0.3) {
                    // Deep cosmic gas (Electric Purple)
                    int rColor = (int) (139 * intensity);
                    int gColor = (int) (92 * intensity);
                    int bColor = (int) (246 * intensity);
                    int col = (rColor << 16) | (gColor << 8) | bColor;
                    canvas.writeCell(x, y, '▒', col, 0x000002);
                } else {
                    // Background dark void
                    if ((x * 13 + y * 37) % 59 == 0 && intensity > 0.15) {
                        canvas.writeCell(x, y, '·', 0x475569, 0x000000);
                    } else {
                        canvas.writeCell(x, y, ' ', 0x000000, 0x000000);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🌌 Deep Space Nebula Star Nest";
    }
}
