package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * @class AttractorEffect
 * @brief 🌀 Lorenz Attractor Particle Swarm simulator.
 * 
 * Computes Lorenz system chaotic differential physics equations in 3D:
 * - dx/dt = Sigma * (y - x)
 * - dy/dt = x * (Rho - z) - y
 * - dz/dt = x * y - Beta * z
 * Projects coordinates onto the terminal viewport using dynamic trailing particle glyphs.
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

    /**
     * @brief Initializes view sizes and spawns particles spread out near the attractor core.
     * 
     * @param width Terminal screen width.
     * @param height Terminal screen height.
     */
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

    /**
     * @brief Computes one step of the Lorenz system derivatives using forward Euler integration.
     * 
     * @param time Total elapsed time in seconds.
     * @param deltaTime Elapsed time in seconds since last frame.
     */
    @Override
    public void update(double time, double deltaTime) {
        // Compute speed factor based on 120 FPS target to preserve physics integration speed
        double speedFactor = deltaTime * 120.0;
        double currentDT = DT * speedFactor;

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            // 1. Evaluate Lorenz differential equations
            double dx = SIGMA * (py[i] - px[i]);
            double dy = px[i] * (RHO - pz[i]) - py[i];
            double dz = px[i] * py[i] - BETA * pz[i];

            px[i] += dx * currentDT;
            py[i] += dy * currentDT;
            pz[i] += dz * currentDT;
        }
    }

    /**
     * @brief Renders the Lorenz trail indices using high-precision scaling and projection.
     * 
     * Uses neon gradient interpolations down the particle indexes for trailing glows.
     * 
     * @param canvas Double-buffer render target.
     */
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

    /**
     * @brief Returns the visual user-friendly name of the effect.
     * @return String effect name label.
     */
    @Override
    public String getName() {
        return "Lorenz Attractor Particle Swarm";
    }
}
