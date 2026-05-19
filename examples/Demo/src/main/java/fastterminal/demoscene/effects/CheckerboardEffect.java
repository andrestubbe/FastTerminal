package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * @class CheckerboardEffect
 * @brief 🏁 Classic Amiga Retro Checkerboard Zoom & Rotate effect.
 * 
 * Performs high-speed 2D affine rotation and dynamic zoom scaling projection.
 * Projects true-color procedural checkered grid tiles onto a double vertical
 * resolution space using terminal half-blocks (▄).
 */
public class CheckerboardEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double angle = 0.0;
    private double zoom = 1.0;

    /**
     * @brief Initializes view dimensions.
     * @param width Terminal screen width.
     * @param height Terminal screen height.
     */
    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * @brief Updates rotation angle and dynamic zoom oscillations.
     * @param frameIndex Monotonically increasing frame index.
     */
    @Override
    public void update(long frameIndex) {
        // Slowed down to exactly 1/4 speed for elegant retro-amiga drifting
        angle = frameIndex * 0.0075;
        // Pulse zoom factor dynamically
        zoom = 0.08 + 0.05 * Math.sin(frameIndex * 0.01);
    }

    private int sampleColor(double dx, double dy, double cosA, double sinA) {
        double rx = dx * cosA - dy * sinA;
        double ry = dx * sinA + dy * cosA;
        rx *= zoom;
        ry *= zoom;
        int gridX = (int) Math.floor(rx);
        int gridY = (int) Math.floor(ry);
        boolean isChecker = ((gridX + gridY) & 1) == 0;

        if (isChecker) {
            int r = (int) (128 + 64 * Math.sin(gridX * 0.2 + angle));
            int g = (int) (50 + 50 * Math.cos(gridY * 0.2 + angle * 0.5));
            int b = (int) (220 + 35 * Math.sin(angle));
            return (Math.max(0, Math.min(255, r)) << 16)
                 | (Math.max(0, Math.min(255, g)) << 8)
                 | Math.max(0, Math.min(255, b));
        } else {
            int r = (int) (20 + 10 * Math.sin(gridY * 0.3));
            int g = 10;
            int b = (int) (40 + 20 * Math.cos(gridX * 0.3));
            return (Math.max(0, Math.min(255, r)) << 16)
                 | (Math.max(0, Math.min(255, g)) << 8)
                 | Math.max(0, Math.min(255, b));
        }
    }

    /**
     * @brief Performs standard affine pixel mapping and flushes double-res subpixels.
     * @param canvas Double-buffer render target.
     */
    @Override
    public void render(FastTerminalScene canvas) {
        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);
        double centerX = width / 2.0;

        for (int row = 0; row < height; row++) {
            int yTop = 2 * row;
            int yBot = 2 * row + 1;

            // In double-resolution half-block space, pixels are square-ish
            double dyTop = yTop - height;
            double dyBot = yBot - height;

            for (int x = 0; x < width; x++) {
                double dx = x - centerX;

                // 1. Calculate top pixel color with 2x2 SSAA
                int rTop = 0, gTop = 0, bTop = 0;
                
                int c1 = sampleColor(dx - 0.25, dyTop - 0.25, cosA, sinA);
                rTop += (c1 >> 16) & 0xFF; gTop += (c1 >> 8) & 0xFF; bTop += c1 & 0xFF;
                
                int c2 = sampleColor(dx + 0.25, dyTop - 0.25, cosA, sinA);
                rTop += (c2 >> 16) & 0xFF; gTop += (c2 >> 8) & 0xFF; bTop += c2 & 0xFF;
                
                int c3 = sampleColor(dx - 0.25, dyTop + 0.25, cosA, sinA);
                rTop += (c3 >> 16) & 0xFF; gTop += (c3 >> 8) & 0xFF; bTop += c3 & 0xFF;
                
                int c4 = sampleColor(dx + 0.25, dyTop + 0.25, cosA, sinA);
                rTop += (c4 >> 16) & 0xFF; gTop += (c4 >> 8) & 0xFF; bTop += c4 & 0xFF;
                
                int colorTop = ((rTop >> 2) << 16) | ((gTop >> 2) << 8) | (bTop >> 2);

                // 2. Calculate bottom pixel color with 2x2 SSAA
                int rBot = 0, gBot = 0, bBot = 0;
                
                int c5 = sampleColor(dx - 0.25, dyBot - 0.25, cosA, sinA);
                rBot += (c5 >> 16) & 0xFF; gBot += (c5 >> 8) & 0xFF; bBot += c5 & 0xFF;
                
                int c6 = sampleColor(dx + 0.25, dyBot - 0.25, cosA, sinA);
                rBot += (c6 >> 16) & 0xFF; gBot += (c6 >> 8) & 0xFF; bBot += c6 & 0xFF;
                
                int c7 = sampleColor(dx - 0.25, dyBot + 0.25, cosA, sinA);
                rBot += (c7 >> 16) & 0xFF; gBot += (c7 >> 8) & 0xFF; bBot += c7 & 0xFF;
                
                int c8 = sampleColor(dx + 0.25, dyBot + 0.25, cosA, sinA);
                rBot += (c8 >> 16) & 0xFF; gBot += (c8 >> 8) & 0xFF; bBot += c8 & 0xFF;
                
                int colorBot = ((rBot >> 2) << 16) | ((gBot >> 2) << 8) | (bBot >> 2);

                // Write half-block cell: Foreground represents bottom pixel, Background represents top pixel
                canvas.writeCell(x, row, '▄', colorBot, colorTop);
            }
        }
    }

    /**
     * @brief Returns the visual user-friendly name of the effect.
     * @return String effect name label.
     */
    @Override
    public String getName() {
        return "Amiga Checkerboard Zoom & Rotate";
    }
}
