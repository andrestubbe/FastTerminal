package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🌀 Raymarched 3D Voxel Tunnel effect.
 * Maps coordinates to polar coordinates (angle, radius) to simulate deep 3D perspective flight.
 */
public class TunnelEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double time = 0.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        time = frameIndex * 0.05;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        double centerX = width / 2.0;
        double centerY = height / 2.0;

        // Camera panning effect
        double panX = 4.0 * Math.sin(time * 0.5);
        double panY = 2.0 * Math.cos(time * 0.7);

        for (int y = 0; y < height; y++) {
            double dy = (y - centerY - panY) * 2.0; // Aspect ratio adjustment

            for (int x = 0; x < width; x++) {
                double dx = x - centerX - panX;

                // 1. Calculate distance (depth projection)
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 0.1) {
                    canvas.writeCell(x, y, ' ', 0, 0x000000);
                    continue;
                }

                // 2. Polar transformation
                double z = 25.0 / dist; // Depth
                double angle = Math.atan2(dy, dx); // Angle around center

                // 3. Compute repeating tunnel texture coordinates
                double u = (angle + time * 0.1) * (8.0 / Math.PI); // Spiral rotation
                double v = z + time * 2.5; // Flight translation

                int gridU = (int) Math.floor(u);
                int gridV = (int) Math.floor(v);

                // Checkerboard voxel tunnel ribs
                boolean isRib = (gridV % 8 == 0 || gridV % 8 == 1);
                boolean isStripe = (gridU % 6 == 0 || gridU % 6 == 1);

                // Depth shading factor (fades to dark in the center)
                double depthShade = Math.min(1.0, dist / (width * 0.45));

                if (isRib || isStripe) {
                    // Pulsing golden ring color with depth shading
                    int r = (int) ((245 + 10 * Math.sin(time)) * depthShade);
                    int g = (int) ((158 + 40 * Math.cos(time)) * depthShade);
                    int b = (int) (11 * depthShade);
                    int color = (r << 16) | (g << 8) | b;

                    // Choose character based on distance for 3D depth texture scaling
                    char glyph = (depthShade > 0.7) ? '█' : (depthShade > 0.4) ? '▓' : '▒';
                    canvas.writeCell(x, y, glyph, color, 0x020100);
                } else {
                    // Deep neon-cyan neon grid lines far inside
                    boolean isGrid = (gridV % 4 == 0 && gridU % 3 == 0);
                    if (isGrid && depthShade > 0.25) {
                        int r = (int) (0 * depthShade);
                        int g = (int) (185 * depthShade);
                        int b = (int) (225 * depthShade);
                        int color = (r << 16) | (g << 8) | b;
                        canvas.writeCell(x, y, '·', color, 0x000005);
                    } else {
                        canvas.writeCell(x, y, ' ', 0, 0x000000);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🌀 Raymarched 3D Voxel Tunnel";
    }
}
