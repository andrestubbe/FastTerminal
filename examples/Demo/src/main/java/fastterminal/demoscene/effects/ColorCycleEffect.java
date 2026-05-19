package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🌀 Hypnotic Vortex / Concentric Color Cycler effect.
 * Renders high-speed spinning concentric rings that cycle their neon colors,
 * creating an illusion of infinite zoom/depth in the terminal.
 */
public class ColorCycleEffect implements DemosceneEffect {

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
        time = frameIndex * 0.12; // High-speed speed color cycling
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

                double dist = Math.sqrt(dx * dx + dy * dy);

                // Trigonometric ring cycling math
                double phaseVal = dist * 0.25 - time;
                double wave = Math.sin(phaseVal);

                if (wave > 0.15) {
                    // Cyclic palette mapping
                    double hue = (dist * 0.04 + time * 0.08) % 1.0;
                    
                    int r = (int) (128 + 127 * Math.sin(hue * 2.0 * Math.PI));
                    int g = (int) (128 + 127 * Math.sin(hue * 2.0 * Math.PI + 2.0 * Math.PI / 3.0));
                    int b = (int) (128 + 127 * Math.sin(hue * 2.0 * Math.PI + 4.0 * Math.PI / 3.0));
                    int color = (r << 16) | (g << 8) | b;

                    char glyph = (wave > 0.8) ? '█' : (wave > 0.5) ? '▓' : '▒';
                    canvas.writeCell(x, y, glyph, color, 0x030105);
                } else {
                    canvas.writeCell(x, y, ' ', 0, 0x010002);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🌀 Hypnotic Vortex Color Cycler";
    }
}
