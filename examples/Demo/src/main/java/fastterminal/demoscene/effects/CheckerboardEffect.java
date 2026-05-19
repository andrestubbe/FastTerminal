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

                // 1. Calculate top pixel color
                double rxTop = dx * cosA - dyTop * sinA;
                double ryTop = dx * sinA + dyTop * cosA;
                rxTop *= zoom;
                ryTop *= zoom;
                int gridXTop = (int) Math.floor(rxTop);
                int gridYTop = (int) Math.floor(ryTop);
                boolean isCheckerTop = ((gridXTop + gridYTop) & 1) == 0;

                int colorTop;
                if (isCheckerTop) {
                    int r = (int) (128 + 64 * Math.sin(gridXTop * 0.2 + angle));
                    int g = (int) (50 + 50 * Math.cos(gridYTop * 0.2 + angle * 0.5));
                    int b = (int) (220 + 35 * Math.sin(angle));
                    colorTop = (Math.max(0, Math.min(255, r)) << 16)
                             | (Math.max(0, Math.min(255, g)) << 8)
                             | Math.max(0, Math.min(255, b));
                } else {
                    int r = (int) (20 + 10 * Math.sin(gridYTop * 0.3));
                    int g = 10;
                    int b = (int) (40 + 20 * Math.cos(gridXTop * 0.3));
                    colorTop = (Math.max(0, Math.min(255, r)) << 16)
                             | (Math.max(0, Math.min(255, g)) << 8)
                             | Math.max(0, Math.min(255, b));
                }

                // 2. Calculate bottom pixel color
                double rxBot = dx * cosA - dyBot * sinA;
                double ryBot = dx * sinA + dyBot * cosA;
                rxBot *= zoom;
                ryBot *= zoom;
                int gridXBot = (int) Math.floor(rxBot);
                int gridYBot = (int) Math.floor(ryBot);
                boolean isCheckerBot = ((gridXBot + gridYBot) & 1) == 0;

                int colorBot;
                if (isCheckerBot) {
                    int r = (int) (128 + 64 * Math.sin(gridXBot * 0.2 + angle));
                    int g = (int) (50 + 50 * Math.cos(gridYBot * 0.2 + angle * 0.5));
                    int b = (int) (220 + 35 * Math.sin(angle));
                    colorBot = (Math.max(0, Math.min(255, r)) << 16)
                             | (Math.max(0, Math.min(255, g)) << 8)
                             | Math.max(0, Math.min(255, b));
                } else {
                    int r = (int) (20 + 10 * Math.sin(gridYBot * 0.3));
                    int g = 10;
                    int b = (int) (40 + 20 * Math.cos(gridXBot * 0.3));
                    colorBot = (Math.max(0, Math.min(255, r)) << 16)
                             | (Math.max(0, Math.min(255, g)) << 8)
                             | Math.max(0, Math.min(255, b));
                }

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
