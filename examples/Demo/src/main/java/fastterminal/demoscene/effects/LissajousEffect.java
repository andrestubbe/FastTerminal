package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * 🎀 Lissajous Ribbon Trails effect.
 * Generates beautiful woven trails following harmonic mathematical Lissajous curves.
 */
public class LissajousEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double time = 0.0;

    // Smooth coordinate trail class
    private static class TrailPoint {
        int x, y;
        int color;
        char glyph;

        TrailPoint(int x, int y, int color, char glyph) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.glyph = glyph;
        }
    }

    private final List<TrailPoint> history = new ArrayList<>();
    private final int MAX_TRAILS = 250;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
        history.clear();
    }

    @Override
    public void update(long frameIndex) {
        time = frameIndex * 0.04;

        // Coordinates center
        double cx = width / 2.0;
        double cy = height / 2.0;

        // Wave coefficients for multiple interwoven ribbon fibers
        double t1 = time;
        double t2 = time + 0.5;
        double t3 = time + 1.0;

        // 1. Fiber A
        double ax = cx + (cx * 0.8) * Math.sin(3 * t1);
        double ay = cy + (cy * 0.8) * Math.cos(2 * t1);
        int colorA = getRainbowColor(t1);

        // 2. Fiber B
        double bx = cx + (cx * 0.8) * Math.sin(4 * t2);
        double bg = cy + (cy * 0.8) * Math.cos(3 * t2);
        int colorB = getRainbowColor(t2);

        // 3. Fiber C
        double cxPos = cx + (cx * 0.8) * Math.sin(5 * t3);
        double cyPos = cy + (cy * 0.8) * Math.cos(4 * t3);
        int colorC = getRainbowColor(t3);

        // Add to history list with trail glyphs
        history.add(0, new TrailPoint((int) ax, (int) ay, colorA, '█'));
        history.add(0, new TrailPoint((int) bx, (int) bg, colorB, '▓'));
        history.add(0, new TrailPoint((int) cxPos, (int) cyPos, colorC, '▒'));

        // Prune old history points
        while (history.size() > MAX_TRAILS) {
            history.remove(history.size() - 1);
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        // Clear screen with a very soft deep blue color fade
        canvas.clear();

        // Draw trail in reverse order so newer elements overwrite older ones (depth sorting)
        for (int i = history.size() - 1; i >= 0; i--) {
            TrailPoint p = history.get(i);
            if (p.x >= 0 && p.x < width && p.y >= 0 && p.y < height) {
                // Fade color intensity down based on history age
                double fade = 1.0 - ((double) i / history.size());
                int r = (int) (((p.color >> 16) & 0xFF) * fade);
                int g = (int) (((p.color >> 8) & 0xFF) * fade);
                int b = (int) ((p.color & 0xFF) * fade);
                int color = (r << 16) | (g << 8) | b;

                // Adjust glyph weight by age
                char glyph = (fade > 0.8) ? p.glyph : (fade > 0.5) ? '▒' : (fade > 0.25) ? '░' : '·';
                canvas.writeCell(p.x, p.y, glyph, color, 0x010103);
            }
        }
    }

    private int getRainbowColor(double t) {
        int r = (int) (127 + 127 * Math.sin(t));
        int g = (int) (127 + 127 * Math.sin(t + 2.0));
        int b = (int) (127 + 127 * Math.sin(t + 4.0));
        return (r << 16) | (g << 8) | b;
    }

    @Override
    public String getName() {
        return "🎀 Lissajous Mathematical Ribbons";
    }
}
