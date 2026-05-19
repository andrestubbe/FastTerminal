package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 📊 Synthwave Audio Spectrum Visualizer simulation effect.
 * Renders multiple neon equalizer channels jumping dynamically to virtual music,
 * with bouncing ceiling peaks and pulsating stereo rings.
 */
public class AudioVisualizerEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double time = 0.0;

    private static final int BANDS = 18;
    private double[] bandHeight = new double[BANDS];
    private double[] peakHeight = new double[BANDS];

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        for (int i = 0; i < BANDS; i++) {
            bandHeight[i] = 0.0;
            peakHeight[i] = 0.0;
        }
    }

    @Override
    public void update(long frameIndex) {
        time = frameIndex * 0.1;

        // Simulate a bouncing bass beat and high frequencies
        double bassPulse = Math.max(0.0, Math.sin(time * 0.8) * Math.cos(time * 0.4));

        for (int i = 0; i < BANDS; i++) {
            // Virtual frequency calculations
            double target;
            if (i < 4) { // Bass region
                target = 0.2 + bassPulse * 0.7 + Math.sin(time * 1.5 + i) * 0.1;
            } else if (i < 12) { // Mid tones
                target = 0.3 + 0.4 * Math.sin(time * 0.9 + i * 0.5) * Math.cos(time * 0.3);
            } else { // High tones
                target = 0.15 + 0.35 * Math.abs(Math.sin(time * 2.3 + i));
            }

            if (target < 0.05) target = 0.05;
            if (target > 0.95) target = 0.95;

            // Interpolate heights
            bandHeight[i] += (target - bandHeight[i]) * 0.25;

            // Bouncing peak dots
            if (bandHeight[i] > peakHeight[i]) {
                peakHeight[i] = bandHeight[i];
            } else {
                peakHeight[i] -= 0.015; // Gravity drop
            }
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // 1. Draw glowing concentric audio shockwave rings in center backdrop
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double aspect = 2.0;
        double ringRadius = (height * 0.28) + 4.0 * Math.sin(time * 0.8);

        for (int y = 0; y < height; y++) {
            double dy = (y - centerY) * aspect;
            for (int x = 0; x < width; x++) {
                double dx = x - centerX;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (Math.abs(dist - ringRadius) < 1.8) {
                    canvas.writeCell(x, y, '░', 0x8B5CF6, 0x020106); // Violet pulse
                } else {
                    canvas.writeCell(x, y, ' ', 0, 0x020106);
                }
            }
        }

        // 2. Render equalizer spectrum bars
        int bandWidth = Math.max(2, width / BANDS - 1);
        int marginX = (width - BANDS * (bandWidth + 1)) / 2;

        for (int i = 0; i < BANDS; i++) {
            int bh = (int) (bandHeight[i] * (height - 6));
            int ph = (int) (peakHeight[i] * (height - 6));

            int startX = marginX + i * (bandWidth + 1);

            // Draw frequency column
            for (int y = 0; y < height - 4; y++) {
                int screenY = height - 4 - y;

                if (y < bh) {
                    // Equalizer multi-color gradient (Green bottom -> Yellow mid -> Red peak)
                    int color;
                    if (y < (height - 6) * 0.45) {
                        color = 0x10B981; // Green
                    } else if (y < (height - 6) * 0.8) {
                        color = 0xF59E0B; // Gold
                    } else {
                        color = 0xEF4444; // Red
                    }

                    for (int bx = 0; bx < bandWidth; bx++) {
                        canvas.writeCell(startX + bx, screenY, '█', color, 0x020106);
                    }
                }
            }

            // Draw ceiling peak dot
            int peakY = height - 4 - ph;
            if (peakY >= 0 && peakY < height - 3) {
                for (int bx = 0; bx < bandWidth; bx++) {
                    canvas.writeCell(startX + bx, peakY, '▬', 0xEC4899, 0x020106); // Hot Pink peak
                }
            }
        }
    }

    @Override
    public String getName() {
        return "📊 Retrowave Equalizer Spectrum Analyzer";
    }
}
