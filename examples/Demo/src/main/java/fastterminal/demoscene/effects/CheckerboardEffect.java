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
        
        // Edge transition width is proportional to the zoom factor (exactly 1 subpixel step width)
        double edgeWidth = zoom * 0.85;

        for (int row = 0; row < height; row++) {
            int yTop = 2 * row;
            int yBot = 2 * row + 1;

            // In double-resolution half-block space, pixels are square-ish
            double dyTop = yTop - height;
            double dyBot = yBot - height;

            for (int x = 0; x < width; x++) {
                double dx = x - centerX;

                // 1. Calculate top pixel color with anti-aliasing
                double rxTop = dx * cosA - dyTop * sinA;
                double ryTop = dx * sinA + dyTop * cosA;
                rxTop *= zoom;
                ryTop *= zoom;
                
                int gridXTop = (int) Math.floor(rxTop);
                int gridYTop = (int) Math.floor(ryTop);
                boolean isCheckerTop = ((gridXTop + gridYTop) & 1) == 0;

                int rCTop = (int) (128 + 64 * Math.sin(gridXTop * 0.2 + angle));
                int gCTop = (int) (50 + 50 * Math.cos(gridYTop * 0.2 + angle * 0.5));
                int bCTop = (int) (220 + 35 * Math.sin(angle));
                int colorCheckerTop = (Math.max(0, Math.min(255, rCTop)) << 16)
                                    | (Math.max(0, Math.min(255, gCTop)) << 8)
                                    | Math.max(0, Math.min(255, bCTop));

                int rBTop = (int) (20 + 10 * Math.sin(gridYTop * 0.3));
                int gBTop = 10;
                int bBTop = (int) (40 + 20 * Math.cos(gridXTop * 0.3));
                int colorBgTop = (Math.max(0, Math.min(255, rBTop)) << 16)
                               | (Math.max(0, Math.min(255, gBTop)) << 8)
                               | Math.max(0, Math.min(255, bBTop));

                // Calculate distance to grid boundaries
                double fxTop = rxTop - gridXTop;
                double fyTop = ryTop - gridYTop;
                double distXTop = Math.min(fxTop, 1.0 - fxTop);
                double distYTop = Math.min(fyTop, 1.0 - fyTop);

                double blendXTop = Math.min(1.0, distXTop / edgeWidth);
                double blendYTop = Math.min(1.0, distYTop / edgeWidth);
                double alphaTop = blendXTop * blendYTop;
                
                // Soften edges with classic smoothstep
                alphaTop = alphaTop * alphaTop * (3.0 - 2.0 * alphaTop);

                int colorActiveTop = isCheckerTop ? colorCheckerTop : colorBgTop;
                int colorOppositeTop = isCheckerTop ? colorBgTop : colorCheckerTop;

                int rT = (int) (((colorActiveTop >> 16) & 0xFF) * alphaTop + ((colorOppositeTop >> 16) & 0xFF) * (1.0 - alphaTop));
                int gT = (int) (((colorActiveTop >> 8) & 0xFF) * alphaTop + ((colorOppositeTop >> 8) & 0xFF) * (1.0 - alphaTop));
                int bT = (int) ((colorActiveTop & 0xFF) * alphaTop + ((colorOppositeTop & 0xFF) * (1.0 - alphaTop)));
                int colorTop = (rT << 16) | (gT << 8) | bT;

                // 2. Calculate bottom pixel color with anti-aliasing
                double rxBot = dx * cosA - dyBot * sinA;
                double ryBot = dx * sinA + dyBot * cosA;
                rxBot *= zoom;
                ryBot *= zoom;
                
                int gridXBot = (int) Math.floor(rxBot);
                int gridYBot = (int) Math.floor(ryBot);
                boolean isCheckerBot = ((gridXBot + gridYBot) & 1) == 0;

                int rCBot = (int) (128 + 64 * Math.sin(gridXBot * 0.2 + angle));
                int gCBot = (int) (50 + 50 * Math.cos(gridYBot * 0.2 + angle * 0.5));
                int bCBot = (int) (220 + 35 * Math.sin(angle));
                int colorCheckerBot = (Math.max(0, Math.min(255, rCBot)) << 16)
                                    | (Math.max(0, Math.min(255, gCBot)) << 8)
                                    | Math.max(0, Math.min(255, bCBot));

                int rBBot = (int) (20 + 10 * Math.sin(gridYBot * 0.3));
                int gBBot = 10;
                int bBBot = (int) (40 + 20 * Math.cos(gridXBot * 0.3));
                int colorBgBot = (Math.max(0, Math.min(255, rBBot)) << 16)
                               | (Math.max(0, Math.min(255, gBBot)) << 8)
                               | Math.max(0, Math.min(255, bBBot));

                // Calculate distance to grid boundaries
                double fxBot = rxBot - gridXBot;
                double fyBot = ryBot - gridYBot;
                double distXBot = Math.min(fxBot, 1.0 - fxBot);
                double distYBot = Math.min(fyBot, 1.0 - fyBot);

                double blendXBot = Math.min(1.0, distXBot / edgeWidth);
                double blendYBot = Math.min(1.0, distYBot / edgeWidth);
                double alphaBot = blendXBot * blendYBot;
                
                // Soften edges with classic smoothstep
                alphaBot = alphaBot * alphaBot * (3.0 - 2.0 * alphaBot);

                int colorActiveBot = isCheckerBot ? colorCheckerBot : colorBgBot;
                int colorOppositeBot = isCheckerBot ? colorBgBot : colorCheckerBot;

                int rB = (int) (((colorActiveBot >> 16) & 0xFF) * alphaBot + ((colorOppositeBot >> 16) & 0xFF) * (1.0 - alphaBot));
                int gB = (int) (((colorActiveBot >> 8) & 0xFF) * alphaBot + ((colorOppositeBot >> 8) & 0xFF) * (1.0 - alphaBot));
                int bB = (int) ((colorActiveBot & 0xFF) * alphaBot + ((colorOppositeBot & 0xFF) * (1.0 - alphaBot)));
                int colorBot = (rB << 16) | (gB << 8) | bB;

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
