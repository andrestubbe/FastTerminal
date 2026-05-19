package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;
import java.util.Random;

/**
 * 🏀 Kinetic physics sandbox with Bouncing Neon Spheres.
 * Renders multiple heavy balls bouncing inside bounds under gravity with kinetic trails.
 */
public class BouncingBallsEffect implements DemosceneEffect {

    private int width;
    private int height;

    private static final int BALL_COUNT = 8;
    private double[] px = new double[BALL_COUNT];
    private double[] py = new double[BALL_COUNT];
    private double[] vx = new double[BALL_COUNT];
    private double[] vy = new double[BALL_COUNT];
    private double[] radius = new double[BALL_COUNT];
    private int[] color = new int[BALL_COUNT];

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        Random rand = new Random();
        for (int i = 0; i < BALL_COUNT; i++) {
            px[i] = width * 0.2 + rand.nextDouble() * width * 0.6;
            py[i] = height * 0.2 + rand.nextDouble() * height * 0.6;
            vx[i] = (rand.nextDouble() - 0.5) * 1.5;
            vy[i] = -0.5 - rand.nextDouble() * 1.5;
            radius[i] = 3.0 + rand.nextDouble() * 3.5;

            int choice = rand.nextInt(4);
            if (choice == 0) color[i] = 0xEC4899;      // Pink
            else if (choice == 1) color[i] = 0x06B6D4; // Cyan
            else if (choice == 2) color[i] = 0x10B981; // Emerald
            else color[i] = 0xF59E0B;                 // Gold
        }
    }

    @Override
    public void update(long frameIndex) {
        double gravity = 0.08;
        double bounce = -0.85;

        for (int i = 0; i < BALL_COUNT; i++) {
            vy[i] += gravity;
            px[i] += vx[i];
            py[i] += vy[i];

            // Bounce check boundaries
            double rx = radius[i];
            double ry = radius[i] / 2.0; // Aspect ratio adjustment

            if (px[i] - rx < 0) {
                px[i] = rx;
                vx[i] *= bounce;
            } else if (px[i] + rx >= width) {
                px[i] = width - 1 - rx;
                vx[i] *= bounce;
            }

            if (py[i] - ry < 0) {
                py[i] = ry;
                vy[i] *= bounce;
            } else if (py[i] + ry >= height) {
                py[i] = height - 1 - ry;
                vy[i] *= bounce;
                // Add a small rolling friction
                vx[i] *= 0.95;
            }
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        // Draw deep tech backing grid
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((x - y) % 15 == 0 || (x + y) % 15 == 0) {
                    canvas.writeCell(x, y, '·', 0x1E293B, 0x030308);
                } else {
                    canvas.writeCell(x, y, ' ', 0, 0x010103);
                }
            }
        }

        // Draw balls with radial gradients
        double aspect = 2.0;
        for (int i = 0; i < BALL_COUNT; i++) {
            double cx = px[i];
            double cy = py[i];
            double r = radius[i];

            for (int y = (int) (cy - r / aspect - 1); y <= (int) (cy + r / aspect + 1); y++) {
                double dy = (y - cy) * aspect;

                for (int x = (int) (cx - r - 1); x <= (int) (cx + r + 1); x++) {
                    if (x < 0 || x >= width || y < 0 || y >= height) continue;

                    double dx = x - cx;
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist < r) {
                        double shade = 1.0 - (dist / r);
                        // Bevel lighting from top-left
                        double angle = Math.atan2(dy, dx);
                        double highlight = Math.max(0.0, Math.sin(angle - 2.5));
                        double factor = 0.3 + 0.7 * shade + 0.5 * Math.pow(highlight, 4);

                        int cr = (int) (((color[i] >> 16) & 0xFF) * factor);
                        int cg = (int) (((color[i] >> 8) & 0xFF) * factor);
                        int cb = (int) ((color[i] & 0xFF) * factor);

                        // Clamp values safely
                        cr = Math.min(255, cr);
                        cg = Math.min(255, cg);
                        cb = Math.min(255, cb);

                        int finalColor = (cr << 16) | (cg << 8) | cb;
                        char glyph = (factor > 0.8) ? '█' : (factor > 0.5) ? '▓' : (factor > 0.35) ? '▒' : '░';
                        canvas.writeCell(x, y, glyph, finalColor, 0x010103);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🏀 Kinetic Bouncing Neon Spheres";
    }
}
