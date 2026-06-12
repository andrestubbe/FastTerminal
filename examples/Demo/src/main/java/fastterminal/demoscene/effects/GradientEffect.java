package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fasttui.util.Gradient;
import fastterminal.demoscene.DemosceneEffect;

/**
 * @class GradientEffect
 * @brief Animated 24-bit True Color Fluid diagonal gradient & plasma morph fusion.
 * 
 * Implements a synchronized 1200-frame cyclic hold-and-fade system to transition
 * smoothly between pure diagonal fluid gradients and sinusoidal plasma fields.
 * Leverages double vertical subpixel resolution using terminal half-block (▄) mapping.
 */
public class GradientEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double phase = 0.0;
    private double time = 0.0;
    private double blendFactor = 0.0;

    /**
     * @brief Initializes view sizes.
     * @param width Terminal screen width.
     * @param height Terminal screen height.
     */
    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * @brief Computes blending transitions and animates wave phase speeds.
     * 
     * Applies a periodic cycle:
     * - Frames 0-449: Pure diagonal fluid gradients.
     * - Frames 450-549: Linear morph transition.
     * - Frames 550-1049: Sinusoidal plasma waves.
     * - Frames 1050-1199: Inverse morph transition back.
     * 
    /**
     * @brief Computes camera glides and controls image cross-fade timings.
     * 
     * @param time Total elapsed time in seconds.
     * @param deltaTime Elapsed time in seconds since last frame.
     */
    @Override
    public void update(double time, double deltaTime) {
        phase = time * 2.4;
        this.time = time * 3.6; // Controls plasma morph wave velocity

        // 10.0-second cyclic hold-and-fade system
        double cycleTime = time % 10.0;
        if (cycleTime < 3.75) {
            blendFactor = 0.0; // Hold Fluid Gradients
        } else if (cycleTime < 4.5833) {
            blendFactor = (cycleTime - 3.75) / 0.8333; // Smooth 0.8333-second fade from Gradients to Plasma
        } else if (cycleTime < 8.75) {
            blendFactor = 1.0; // Hold Sinusoidal Plasma
        } else {
            // Smooth 0.8333-second fade from Plasma to Gradients (8.75s to 9.5833s), then hold at 0.0 for remaining 0.4167 seconds
            blendFactor = Math.max(0.0, 1.0 - (cycleTime - 8.75) / 0.8333);
        }
        
        // Safety clamp blendFactor to strictly [0.0, 1.0] to prevent any numerical overshoot/undershoot
        blendFactor = Math.max(0.0, Math.min(1.0, blendFactor));
    }

    /**
     * @brief Renders the blended fluid gradients and plasma directly to the canvas using half-blocks.
     * @param canvas Double-buffer render target.
     */
    @Override
    public void render(FastTerminalScene canvas) {
        // Compute diagonal gradient color boundaries
        double t1 = (Math.sin(phase) + 1.0) / 2.0;
        double t2 = (Math.cos(phase * 0.8) + 1.0) / 2.0;
        int bgStart = Gradient.interpolate(0x6366F1, 0xD946EF, t1);
        int bgEnd = Gradient.interpolate(0x06B6D4, 0x14B8A6, t2);

        int doubleHeight = 2 * height;

        for (int row = 0; row < height; row++) {
            int yTop = 2 * row;
            int yBot = 2 * row + 1;

            double cyTop = yTop - height;
            double cyBot = yBot - height;

            for (int x = 0; x < width; x++) {
                double cx = x - width / 2.0;

                // 1. Calculate diagonal gradient subpixel colors
                double factorTop = (double) (x + yTop) / (width + doubleHeight);
                int colorTopGrad = Gradient.interpolate(bgStart, bgEnd, factorTop);

                double factorBot = (double) (x + yBot) / (width + doubleHeight);
                int colorBotGrad = Gradient.interpolate(bgStart, bgEnd, factorBot);

                // 2. Calculate sinusoidal plasma subpixel colors
                double v1Top = Math.sin(x / 12.0 + time);
                double v2Top = Math.sin(yTop / 12.0 + time * 1.3);
                double v3Top = Math.sin((x + yTop) / 24.0 + time);
                double v4Top = Math.sin(Math.sqrt(cx * cx + cyTop * cyTop) / 12.0 - time);
                double totalTop = (v1Top + v2Top + v3Top + v4Top) / 4.0;
                int rTop = (int) (128 + 64 * Math.sin(totalTop * Math.PI + time));
                int gTop = (int) (50 + 50 * Math.cos(totalTop * Math.PI * 0.5 + time * 0.5));
                int bTop = (int) (220 + 35 * Math.sin(time));
                int colorTopPlasma = (Math.max(0, Math.min(255, rTop)) << 16)
                                   | (Math.max(0, Math.min(255, gTop)) << 8)
                                   | Math.max(0, Math.min(255, bTop));

                double v1Bot = Math.sin(x / 12.0 + time);
                double v2Bot = Math.sin(yBot / 12.0 + time * 1.3);
                double v3Bot = Math.sin((x + yBot) / 24.0 + time);
                double v4Bot = Math.sin(Math.sqrt(cx * cx + cyBot * cyBot) / 12.0 - time);
                double totalBot = (v1Bot + v2Bot + v3Bot + v4Bot) / 4.0;
                int rBot = (int) (128 + 64 * Math.sin(totalBot * Math.PI + time));
                int gBot = (int) (50 + 50 * Math.cos(totalBot * Math.PI * 0.5 + time * 0.5));
                int bBot = (int) (220 + 35 * Math.sin(time));
                int colorBotPlasma = (Math.max(0, Math.min(255, rBot)) << 16)
                                   | (Math.max(0, Math.min(255, gBot)) << 8)
                                   | Math.max(0, Math.min(255, bBot));

                // 3. Perform linear interpolation (lerp) blending based on current cycle factor
                int colorTop, colorBot;
                if (blendFactor == 0.0) {
                    colorTop = colorTopGrad;
                    colorBot = colorBotGrad;
                } else if (blendFactor == 1.0) {
                    colorTop = colorTopPlasma;
                    colorBot = colorBotPlasma;
                } else {
                    // Blend Top
                    int rG = (colorTopGrad >> 16) & 0xFF;
                    int gG = (colorTopGrad >> 8) & 0xFF;
                    int bG = colorTopGrad & 0xFF;

                    int rP = (colorTopPlasma >> 16) & 0xFF;
                    int gP = (colorTopPlasma >> 8) & 0xFF;
                    int bP = colorTopPlasma & 0xFF;

                    int rT = (int) (rG + (rP - rG) * blendFactor);
                    int gT = (int) (gG + (gP - gG) * blendFactor);
                    int bT = (int) (bG + (bP - bG) * blendFactor);
                    
                    // Clamp top channels to [0, 255] to prevent 32-bit two's complement sign leakage (white spots)
                    rT = Math.max(0, Math.min(255, rT));
                    gT = Math.max(0, Math.min(255, gT));
                    bT = Math.max(0, Math.min(255, bT));
                    colorTop = (rT << 16) | (gT << 8) | bT;

                    // Blend Bottom
                    int rGB = (colorBotGrad >> 16) & 0xFF;
                    int gGB = (colorBotGrad >> 8) & 0xFF;
                    int bGB = colorBotGrad & 0xFF;

                    int rPB = (colorBotPlasma >> 16) & 0xFF;
                    int gPB = (colorBotPlasma >> 8) & 0xFF;
                    int bPB = colorBotPlasma & 0xFF;

                    int rB = (int) (rGB + (rPB - rGB) * blendFactor);
                    int gB = (int) (gGB + (gPB - gGB) * blendFactor);
                    int bB = (int) (bGB + (bPB - bGB) * blendFactor);
                    
                    // Clamp bottom channels to [0, 255]
                    rB = Math.max(0, Math.min(255, rB));
                    gB = Math.max(0, Math.min(255, gB));
                    bB = Math.max(0, Math.min(255, bB));
                    colorBot = (rB << 16) | (gB << 8) | bB;
                }

                // Write half-block
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
        return "Fluid Gradient & Sinusoidal Plasma Fusion";
    }
}
