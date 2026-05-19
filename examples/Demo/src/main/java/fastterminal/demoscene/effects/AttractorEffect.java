package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🌀 Lorenz Attractor Particle Swarm effect.
 * Simulates chaotic differential equations in 3D, projecting a swarm of glowing
 * physics particles that orbit the Lorenz attractor core in real-time.
 */
public class AttractorEffect implements DemosceneEffect {

    private int width;
    private int height;

    private static final int PARTICLE_COUNT = 150;
    private double[] px = new double[PARTICLE_COUNT];
    private double[] py = new double[PARTICLE_COUNT];
    private double[] pz = new double[PARTICLE_COUNT];

    // Lorenz Attractor Parameters
    private static final double SIGMA = 10.0;
    private static final double RHO = 28.0;
    private static final double BETA = 8.0 / 3.0;
    private static final double DT = 0.008;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        // Initialize particles slightly spread out near the core
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            px[i] = 0.1 + i * 0.05;
            py[i] = 0.0;
            pz[i] = 20.0 + i * 0.1;
        }
    }

    @Override
    public void update(long frameIndex) {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            // 1. Evaluate Lorenz differential equations
            double dx = SIGMA * (py[i] - px[i]);
            double dy = px[i] * (RHO - pz[i]) - py[i];
            double dz = px[i] * py[i] - BETA * pz[i];

            px[i] += dx * DT;
            py[i] += dy * DT;
            pz[i] += dz * DT;
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // Background space dust
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                canvas.writeCell(x, y, ' ', 0, 0x020105);
            }
        }

        double aspect = 2.1; // aspect ratio correction

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            // Translate and scale coordinates to fit screen
            double sx = px[i] * 1.35;
            double sy = py[i] * 0.65;
            double sz = pz[i];

            int screenX = (int) (width / 2.0 + sx * aspect);
            int screenY = (int) (height * 0.55 - (sz - 25.0) * 0.65);

            if (screenX >= 0 && screenX < width && screenY >= 0 && screenY < height) {
                // Color cycles dynamically down the trail Index
                double t = (double) i / PARTICLE_COUNT;
                int r = (int) (236 * t + 6 * (1.0 - t));
                int g = (int) (72 * t + 182 * (1.0 - t));
                int b = (int) (153 * t + 212 * (1.0 - t));
                int color = (r << 16) | (g << 8) | b;

                char glyph = (i % 3 == 0) ? '✦' : (i % 3 == 1) ? '•' : '·';
                canvas.writeCell(screenX, screenY, glyph, color, 0x010003);
            }
        }
    }

    @Override
    public String getName() {
        return "🌀 Lorenz Attractor Particle Swarm";
    }
}
