package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.Gradient;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🎨 Animated 24-bit True Color Fluid diagonal gradient effect.
 */
public class GradientEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double phase = 0.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        phase = frameIndex * 0.02;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        // 1. Interpolate diagonal gradients dynamically
        double t1 = (Math.sin(phase) + 1.0) / 2.0;
        double t2 = (Math.cos(phase * 0.8) + 1.0) / 2.0;

        // Violet -> Hot Pink
        int bgStart = Gradient.interpolate(0x6366F1, 0xD946EF, t1);
        // Deep Cyan -> Emerald Green
        int bgEnd = Gradient.interpolate(0x06B6D4, 0x14B8A6, t2);

        Gradient.applyDiagonalBg(canvas, 0, 0, width, height, bgStart, bgEnd);

        // 2. Render floating elegant title card
        int titleY = height / 2 - 1;
        if (width >= 15 && height >= 5) {
            String titleText = width >= 36 ? " [ FASTTERMINAL 24-BIT GRADIENTS ] " : " [ GRADIENTS ] ";
            int titleX = (width - titleText.length()) / 2;
            canvas.writeString(titleX, titleY, titleText, 0x000000, 0xFFCC00);

            if (width >= 50 && height >= 8) {
                String subtitle = " Buttery-Smooth 120 FPS | Zero-GC Swapchain ";
                int subX = (width - subtitle.length()) / 2;
                canvas.writeString(subX, Math.min(titleY + 1, height - 1), subtitle, 0xFFFFFF, 0x222222);
            }
        }
    }

    @Override
    public String getName() {
        return "🎨 Animated 24-bit Diagonal Fluid Gradients";
    }
}
