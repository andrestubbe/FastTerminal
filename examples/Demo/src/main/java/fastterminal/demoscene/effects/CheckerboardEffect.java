package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🏁 Amiga Retro Checkerboard Zoom & Rotate effect.
 * Performs real-time rotation and zoom mapping on a 2D viewport.
 */
public class CheckerboardEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double angle = 0.0;
    private double zoom = 1.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        // Rotate smoothly over time
        angle = frameIndex * 0.03;
        // Pulse zoom factor dynamically
        zoom = 0.08 + 0.05 * Math.sin(frameIndex * 0.04);
    }

    @Override
    public void render(FastTerminalScene canvas) {
        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);
        double centerX = width / 2.0;
        double centerY = height / 2.0;

        for (int y = 0; y < height; y++) {
            // Adjust vertical character aspect ratio (roughly 2:1 height/width)
            double dy = (y - centerY) * 2.0;

            for (int x = 0; x < width; x++) {
                double dx = x - centerX;

                // Rotate
                double rx = dx * cosA - dy * sinA;
                double ry = dx * sinA + dy * cosA;

                // Zoom
                rx *= zoom;
                ry *= zoom;

                // Discrete grid coordinates
                int gridX = (int) Math.floor(rx);
                int gridY = (int) Math.floor(ry);

                boolean isChecker = ((gridX + gridY) & 1) == 0;

                if (isChecker) {
                    // Pulsing neon purple pattern
                    int r = (int) (128 + 64 * Math.sin(gridX * 0.2 + angle));
                    int g = (int) (50 + 50 * Math.cos(gridY * 0.2 + angle * 0.5));
                    int b = (int) (220 + 35 * Math.sin(angle));
                    int fg = (r << 16) | (g << 8) | b;
                    canvas.writeCell(x, y, '█', fg, 0x0C041C);
                } else {
                    // Deep space background
                    int r = (int) (20 + 10 * Math.sin(gridY * 0.3));
                    int g = 10;
                    int b = (int) (40 + 20 * Math.cos(gridX * 0.3));
                    int bg = (r << 16) | (g << 8) | b;
                    canvas.writeCell(x, y, ' ', 0x000000, bg);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🏁 Amiga Checkerboard Zoom & Rotate";
    }
}
