package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;
import java.util.Random;

/**
 * 🌋 Active Volcano Particle Plume effect.
 * Simulates physics wind, gravity, and drag on 150 red-hot lava spark particles
 * erupting continuously from the volcano crater bottom.
 */
public class VolcanoEffect implements DemosceneEffect {

    private int width;
    private int height;

    private static final int SPARK_COUNT = 150;
    private double[] px = new double[SPARK_COUNT];
    private double[] py = new double[SPARK_COUNT];
    private double[] vx = new double[SPARK_COUNT];
    private double[] vy = new double[SPARK_COUNT];
    private int[] age = new int[SPARK_COUNT];
    private int[] maxAge = new int[SPARK_COUNT];

    private Random rand = new Random();

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        for (int i = 0; i < SPARK_COUNT; i++) {
            resetSpark(i);
            // Stagger starting age
            age[i] = rand.nextInt(maxAge[i]);
        }
    }

    private void resetSpark(int idx) {
        px[idx] = width / 2.0 + (rand.nextDouble() - 0.5) * 4.0;
        py[idx] = height - 3.0;
        vx[idx] = (rand.nextDouble() - 0.5) * 0.9;
        vy[idx] = -0.4 - rand.nextDouble() * 0.8;
        age[idx] = 0;
        maxAge[idx] = 40 + rand.nextInt(40);
    }

    @Override
    public void update(long frameIndex) {
        double gravity = 0.015;
        // Dynamic horizontal cross-wind blowing left/right periodically
        double wind = 0.012 * Math.sin(frameIndex * 0.02);

        for (int i = 0; i < SPARK_COUNT; i++) {
            age[i]++;
            if (age[i] >= maxAge[i]) {
                resetSpark(i);
            } else {
                vy[i] += gravity;
                vx[i] += wind;
                px[i] += vx[i];
                py[i] += vy[i];
            }
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // 1. Draw static volcanic mountain crater silhouette
        int craterY = height - 4;
        for (int y = craterY; y < height; y++) {
            int indent = y - craterY;
            int leftEdge = width / 2 - 8 - indent * 3;
            int rightEdge = width / 2 + 8 + indent * 3;

            for (int x = 0; x < width; x++) {
                if (x >= leftEdge && x <= rightEdge) {
                    canvas.writeCell(x, y, '█', 0x1E293B, 0x05050A); // Dark lava rock
                } else {
                    canvas.writeCell(x, y, ' ', 0, 0x020208);
                }
            }
        }

        // Draw ambient void
        for (int y = 0; y < craterY; y++) {
            for (int x = 0; x < width; x++) {
                canvas.writeCell(x, y, ' ', 0, 0x020208);
            }
        }

        // 2. Render active plume lava sparks
        for (int i = 0; i < SPARK_COUNT; i++) {
            int sx = (int) Math.round(px[i]);
            int sy = (int) Math.round(py[i]);

            if (sx >= 0 && sx < width && sy >= 0 && sy < height) {
                double t = (double) age[i] / maxAge[i];
                
                int color;
                if (t < 0.25) { // Super hot core (Yellowish white)
                    color = 0xFDE047;
                } else if (t < 0.6) { // Molten hot (Vibrant red-orange)
                    color = 0xEA580C;
                } else { // Fading coal (Dark ash red)
                    color = 0x7F1D1D;
                }

                char glyph = (t < 0.3) ? '✺' : (t < 0.7) ? '•' : '·';
                canvas.writeCell(sx, sy, glyph, color, 0x020208);
            }
        }
    }

    @Override
    public String getName() {
        return "🌋 Lava Volcano Particle Plume";
    }
}
