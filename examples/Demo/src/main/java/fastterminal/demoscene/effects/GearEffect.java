package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * ⚙️ Intermeshing Gear Rotation effect.
 * Renders multiple mechanical metallic gears of different sizes intermeshed together,
 * spinning at mathematically matched speeds, over an blueprint grid.
 */
public class GearEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double rotation = 0.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        rotation = frameIndex * 0.03;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // 1. Draw a technical blueprints background grid
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x % 10 == 0 || y % 5 == 0) {
                    canvas.writeCell(x, y, '┼', 0x1E3A8A, 0x050515); // Blueprint blue grid lines
                } else {
                    canvas.writeCell(x, y, ' ', 0, 0x02020A);
                }
            }
        }

        // 2. Define the three gears (x, y, radius, toothCount, baseSpeedMultiplier, color, characters)
        // Center Large Gear
        drawGear(canvas, width * 0.35, height * 0.5, 16.0, 12, rotation, 0xF59E0B, "█");
        
        // Upper Right Gear (intermeshed, spins opposite, matched teeth speed ratio)
        drawGear(canvas, width * 0.65, height * 0.3, 10.0, 8, -rotation * 1.5 + 0.35, 0x10B981, "▓");

        // Lower Right Gear (intermeshed, spins opposite, matched teeth speed ratio)
        drawGear(canvas, width * 0.68, height * 0.72, 8.0, 6, -rotation * 2.0 + 0.1, 0x3B82F6, "▒");
    }

    private void drawGear(FastTerminalScene canvas, double cx, double cy, double baseRadius, int teeth, double angle, int color, String glyphChar) {
        char glyph = glyphChar.charAt(0);
        double aspect = 2.0; // character cell height-to-width ratio

        for (int y = 0; y < height; y++) {
            double dy = (y - cy) * aspect;

            for (int x = 0; x < width; x++) {
                double dx = x - cx;

                double r = Math.sqrt(dx * dx + dy * dy);
                if (r > baseRadius * 1.4) continue; // Out of bounds skip

                double theta = Math.atan2(dy, dx);

                // Math function of tooth amplitude based on polar angle
                double toothAmplitude = 2.2 * Math.sin(teeth * theta + angle);
                double currentRadiusLimit = baseRadius + toothAmplitude;

                // Center axle hole
                if (r < 2.0) {
                    canvas.writeCell(x, y, '○', 0x94A3B8, 0x0D0D1F);
                }
                // Gear body & teeth
                else if (r < currentRadiusLimit) {
                    // Shading from top-left to give metallic bevels
                    double bevel = 0.5 + 0.5 * Math.sin(theta - 2.5);
                    int br = (int) (((color >> 16) & 0xFF) * bevel);
                    int bg = (int) (((color >> 8) & 0xFF) * bevel);
                    int bb = (int) ((color & 0xFF) * bevel);
                    int finalColor = (br << 16) | (bg << 8) | bb;

                    canvas.writeCell(x, y, glyph, finalColor, 0x050515);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "⚙️ Intermeshing Mechanical Gears";
    }
}
