package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🌪️ Classic Amiga 3D Twister effect.
 * Renders a vertically twisting banner rotating with horizontal sine distortions and metallic shading.
 */
public class TwisterEffect implements DemosceneEffect {

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
        rotation = frameIndex * 0.05;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        int centerX = width / 2;

        for (int y = 0; y < height; y++) {
            // Twist angle shifts dynamically down the vertical screen space
            double angle = rotation + y * 0.12;

            // Twister width and perspective sweep
            double twisterWidth = 14.0 + 4.0 * Math.sin(rotation * 0.3 + y * 0.05);

            // Compute projection of the four sides of the twister column
            for (int side = 0; side < 4; side++) {
                double sideAngle = angle + side * (Math.PI / 2.0);

                double xStart = centerX + twisterWidth * Math.sin(sideAngle);
                double xEnd = centerX + twisterWidth * Math.sin(sideAngle + Math.PI / 2.0);

                if (xStart > xEnd) {
                    double temp = xStart; xStart = xEnd; xEnd = temp;
                }

                // Cull sides pointing away
                double normalZ = Math.cos(sideAngle + Math.PI / 4.0);
                if (normalZ <= 0) continue;

                // Metallic gradient shading on side
                for (int x = (int) Math.round(xStart); x <= (int) Math.round(xEnd); x++) {
                    if (x >= 0 && x < width) {
                        double t = (x - xStart) / (xEnd - xStart);
                        // Highlight in center
                        double shade = Math.sin(t * Math.PI) * normalZ;

                        int r, g, b;
                        if (side == 0) { // Golden bronze side
                            r = (int) (245 * shade);
                            g = (int) (158 * shade);
                            b = (int) (11 * shade);
                        } else if (side == 1) { // Deep cobalt blue side
                            r = (int) (29 * shade);
                            g = (int) (78 * shade);
                            b = (int) (216 * shade);
                        } else if (side == 2) { // Electric pink side
                            r = (int) (236 * shade);
                            g = (int) (72 * shade);
                            b = (int) (153 * shade);
                        } else { // Forest emerald side
                            r = (int) (16 * shade);
                            g = (int) (185 * shade);
                            b = (int) (129 * shade);
                        }

                        int color = (r << 16) | (g << 8) | b;
                        char glyph = (shade > 0.8) ? '█' : (shade > 0.5) ? '▓' : (shade > 0.2) ? '▒' : '░';
                        canvas.writeCell(x, y, glyph, color, 0x05050A);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🌪️ Classic Amiga 3D Twister Column";
    }
}
