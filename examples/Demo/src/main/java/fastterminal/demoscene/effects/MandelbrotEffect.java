package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🌀 Real-time Mandelbrot Fractal Zoom effect.
 * Projects coordinate plane to complex space, iterates equations to check escape velocity,
 * and zooms in deeper dynamically over time with neon color-cycling.
 */
public class MandelbrotEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double zoom = 1.0;
    private double centerRe = -0.743643887037158704752191506114774;
    private double centerIm = 0.131825904205311970493132056385139;
    private double phase = 0.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        // Zoom in exponentially
        zoom = 1.0 + Math.pow(1.09, frameIndex % 150);
        phase = frameIndex * 0.05;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        int maxIter = 32;

        // Dynamic boundaries based on zoom factor
        double w = 3.5 / zoom;
        double h = 2.0 / zoom;

        double minRe = centerRe - w / 2.0;
        double minIm = centerIm - h / 2.0;

        for (int y = 0; y < height; y++) {
            double cIm = minIm + (y * h) / height;

            for (int x = 0; x < width; x++) {
                double cRe = minRe + (x * w) / width;

                // Mandelbrot iteration formula: z = z^2 + c
                double zRe = cRe;
                double zIm = cIm;
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
                    // Core is solid black
                    canvas.writeCell(x, y, ' ', 0, 0x000000);
                } else {
                    // Spectrum HSL cycle color-mapping
                    double t = (double) iter / maxIter;
                    int r = (int) (128 + 127 * Math.sin(t * 8.0 + phase));
                    int g = (int) (64 + 64 * Math.cos(t * 12.0 + phase * 0.5));
                    int b = (int) (180 + 75 * Math.sin(t * 6.0 + phase * 1.5));
                    int color = (r << 16) | (g << 8) | b;

                    // Textures and shapes by iteration bands
                    char glyph = (iter % 3 == 0) ? '█' : (iter % 3 == 1) ? '▓' : '▒';
                    canvas.writeCell(x, y, glyph, color, 0x020005);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🌀 Mandelbrot Real-Time Fractal Zoom";
    }
}
