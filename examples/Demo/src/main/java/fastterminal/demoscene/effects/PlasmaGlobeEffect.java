package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;
import java.util.Random;

/**
 * 🔮 Glowing Electrostatic Plasma Globe effect.
 * Animates high-voltage neon lightning beams cracking out from a center electrode
 * towards the boundary of a glowing circular glass sphere.
 */
public class PlasmaGlobeEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double time = 0.0;
    private Random rand = new Random();

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        time = frameIndex * 0.12;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double aspect = 2.0;
        double globeRadius = height * 0.42;

        // 1. Draw outer glowing glass dome boundary and background
        for (int y = 0; y < height; y++) {
            double dy = (y - centerY) * aspect;
            for (int x = 0; x < width; x++) {
                double dx = x - centerX;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (Math.abs(dist - globeRadius) < 1.0) {
                    canvas.writeCell(x, y, '╬', 0x06B6D4, 0x02010A); // Cyber cyan glass rim
                } else {
                    canvas.writeCell(x, y, ' ', 0, 0x02010A);
                }
            }
        }

        // 2. Draw central electrode core
        int cx = (int) Math.round(centerX);
        int cy = (int) Math.round(centerY);
        canvas.writeCell(cx, cy, '❂', 0xEC4899, 0x02010A); // Glowing Magenta core
        canvas.writeCell(cx - 1, cy, '░', 0x8B5CF6, 0x02010A);
        canvas.writeCell(cx + 1, cy, '░', 0x8B5CF6, 0x02010A);

        // 3. Cast electrostatic lightning beams (3 branching arcs)
        int beamCount = 4;
        for (int b = 0; b < beamCount; b++) {
            // Target angle on globe circumference rotates slowly over time
            double targetAngle = time * 0.5 + b * (2.0 * Math.PI / beamCount) + 0.3 * Math.sin(time * 1.5 + b);
            double targetX = centerX + Math.cos(targetAngle) * globeRadius;
            double targetY = centerY + Math.sin(targetAngle) * globeRadius / aspect;

            // Draw crooked lightning path from center to target
            double curX = centerX;
            double curY = centerY;
            int steps = 18;

            for (int s = 1; s <= steps; s++) {
                double t = (double) s / steps;
                // Linear interpolation with added chaotic noise (crooked lightning!)
                double nextX = centerX + t * (targetX - centerX) + (rand.nextDouble() - 0.5) * 1.8;
                double nextY = centerY + t * (targetY - centerY) + (rand.nextDouble() - 0.5) * 0.9;

                int px = (int) Math.round(curX);
                int py = (int) Math.round(curY);

                if (px >= 0 && px < width && py >= 0 && py < height) {
                    // Core violet-pink lightning color
                    int color = (s % 2 == 0) ? 0xEC4899 : 0x8B5CF6;
                    char glyph = (Math.abs(nextX - curX) > Math.abs(nextY - curY)) ? '═' : '║';
                    canvas.writeCell(px, py, glyph, color, 0x02010A);
                }

                curX = nextX;
                curY = nextY;
            }
        }
    }

    @Override
    public String getName() {
        return "🔮 Electrostatic Plasma Spark Globe";
    }
}
