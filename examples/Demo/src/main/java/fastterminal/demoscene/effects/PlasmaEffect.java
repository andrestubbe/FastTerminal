package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * High-performance 24-bit Sinusoidal Color Plasma fragment wave generator.
 */
public class PlasmaEffect implements DemosceneEffect {

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
        this.time += 0.05; // Control morphing wave velocity
    }

    @Override
    public void render(FastTerminalScene canvas) {
        for (int y = 0; y < height; y++) {
            double cy = y - height / 2.0;
            for (int x = 0; x < width; x++) {
                double cx = x - width / 2.0;

                // Composite multi-frequency interference waves
                double v1 = Math.sin(x / 12.0 + time);
                double v2 = Math.sin(y / 6.0 + time * 1.3);
                double v3 = Math.sin((x + y) / 12.0 + time);
                double v4 = Math.sin(Math.sqrt(cx * cx + cy * cy) / 6.0 - time);

                double total = (v1 + v2 + v3 + v4) / 4.0; // Normalize between -1.0 and 1.0

                // Map wave values to a shifting rainbow spectrum (Hue shift)
                double hue = (total + 1.0) / 2.0;
                int rgb = hslToRgb(hue, 1.0, 0.5);

                canvas.writeCell(x, y, '█', rgb, 0x050505);
            }
        }
    }

    @Override
    public String getName() {
        return "🌀 SINUSOIDAL COLOR PLASMA";
    }

    /**
     * Converts HSL color values to absolute 24-bit RGB packed format.
     */
    private int hslToRgb(double h, double s, double l) {
        double r, g, b;
        if (s == 0) {
            r = g = b = l; // achromatic
        } else {
            double q = l < 0.5 ? l * (1.0 + s) : l + s - l * s;
            double p = 2.0 * l - q;
            r = hueToRgb(p, q, h + 1.0 / 3.0);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1.0 / 3.0);
        }
        int ri = (int) (r * 255.0) & 0xFF;
        int gi = (int) (g * 255.0) & 0xFF;
        int bi = (int) (b * 255.0) & 0xFF;
        return (ri << 16) | (gi << 8) | bi;
    }

    private double hueToRgb(double p, double q, double t) {
        if (t < 0.0) t += 1.0;
        if (t > 1.0) t -= 1.0;
        if (t < 1.0 / 6.0) return p + (q - p) * 6.0 * t;
        if (t < 1.0 / 2.0) return q;
        if (t < 2.0 / 3.0) return p + (q - p) * (2.0 / 3.0 - t) * 6.0;
        return p;
    }
}
