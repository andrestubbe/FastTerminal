package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🌊 3D Fluid Wave Grid Vector effect.
 * Renders a perspective-warping mesh grid representing dynamic liquid heights and valleys.
 */
public class FluidGridEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double phase = 0.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        phase = frameIndex * 0.04;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // Background obsidian void
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                canvas.writeCell(x, y, ' ', 0, 0x020208);
            }
        }

        // Project a grid of rows and columns (24x16 grid mesh size)
        int gridX = 28;
        int gridY = 16;
        double aspect = 2.0;

        for (int gy = 0; gy < gridY; gy++) {
            double normalizedY = (double) gy / (gridY - 1) - 0.5; // [-0.5, 0.5]

            for (int gx = 0; gx < gridX; gx++) {
                double normalizedX = (double) gx / (gridX - 1) - 0.5; // [-0.5, 0.5]

                // Fluid heights wave calculation
                double waveVal = Math.sin(normalizedX * 6.0 + phase) * Math.cos(normalizedY * 6.0 + phase * 0.7);
                double heightOffset = waveVal * 0.35;

                // 3D coordinates
                double x3 = normalizedX * 2.5;
                double y3 = heightOffset;
                double z3 = normalizedY * 2.0 + 3.0; // Distance depth

                // Perspective projection
                double scale = (height * 0.6) / z3;
                int screenX = (int) (width / 2.0 + x3 * scale * aspect);
                int screenY = (int) (height * 0.65 + y3 * scale);

                if (screenX >= 0 && screenX < width && screenY >= 0 && screenY < height) {
                    // Map depth to neon colors (closer = cyan/emerald, further = deep purple)
                    double t = (z3 - 1.0) / 4.0; // depth normalized
                    t = Math.max(0.0, Math.min(1.0, t));

                    int r = (int) (6 * (1.0 - t) + 139 * t);
                    int g = (int) (182 * (1.0 - t) + 92 * t);
                    int b = (int) (212 * (1.0 - t) + 246 * t);
                    int color = (r << 16) | (g << 8) | b;

                    char glyph = (waveVal > 0.6) ? '▲' : (waveVal < -0.6) ? '▼' : '+';
                    canvas.writeCell(screenX, screenY, glyph, color, 0x020208);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🌊 Perspective 3D Fluid Grid Waves";
    }
}
