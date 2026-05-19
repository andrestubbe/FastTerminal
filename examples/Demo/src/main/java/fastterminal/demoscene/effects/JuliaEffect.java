package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🌀 Animated Julia Set Fractal effect.
 * Iterates complex number sequence z = z^2 + c, cycling the complex parameter c
 * along a continuous trigonometric path to create morphing shapes.
 */
public class JuliaEffect implements DemosceneEffect {

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
        time = frameIndex * 0.02;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        int maxIter = 24;

        // Path of the complex constant C
        double cRe = -0.7 + 0.27 * Math.sin(time * 0.5);
        double cIm = 0.27015 + 0.15 * Math.cos(time * 0.8);

        double w = 3.0;
        double h = 2.0;

        for (int y = 0; y < height; y++) {
            double zIm = -h / 2.0 + (y * h) / height;

            for (int x = 0; x < width; x++) {
                double zRe = -w / 2.0 + (x * w) / width;

                int iter = 0;
                while (iter < maxIter) {
                    double zRe2 = zRe * zRe;
                    double zIm2 = zIm * zIm;

                    if (zRe2 + zIm2 > 4.0) break;

                    zIm = 2.0 * zRe * zIm + cIm;
                    zRe = zRe2 - zIm2 + cRe;
                    iter++;
                }

                if (iter == maxIter) {
                    canvas.writeCell(x, y, ' ', 0, 0x000000);
                } else {
                    double t = (double) iter / maxIter;
                    int r = (int) (100 + 155 * t);
                    int g = (int) (50 * (1.0 - t));
                    int b = (int) (150 + 105 * Math.sin(t * Math.PI + time));
                    int color = (r << 16) | (g << 8) | b;

                    char glyph = (iter % 3 == 0) ? '█' : (iter % 3 == 1) ? '▓' : '▒';
                    canvas.writeCell(x, y, glyph, color, 0x030008);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🌀 Morphing Julia Set Fractal";
    }
}
