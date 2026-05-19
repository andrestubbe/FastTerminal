package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;
import java.util.Random;

/**
 * 💧 Real-time Fluid & Gas particle dynamics simulation.
 * Computes SPH (Smoothed-particle hydrodynamics) forces, gravity, and viscosities
 * on 100 fluid nodes, rendering their high-density clusters with radiant colors.
 */
public class FluidDynamicsEffect implements DemosceneEffect {

    private int width;
    private int height;

    private static final int PARTICLE_COUNT = 90;
    private double[] px = new double[PARTICLE_COUNT];
    private double[] py = new double[PARTICLE_COUNT];
    private double[] vx = new double[PARTICLE_COUNT];
    private double[] vy = new double[PARTICLE_COUNT];
    private double[] density = new double[PARTICLE_COUNT];

    private Random rand = new Random();

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        // Spawn particles in a compact cluster block ready to flow
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            px[i] = width * 0.4 + rand.nextDouble() * width * 0.2;
            py[i] = height * 0.1 + rand.nextDouble() * height * 0.3;
            vx[i] = (rand.nextDouble() - 0.5) * 0.5;
            vy[i] = 0.0;
        }
    }

    @Override
    public void update(long frameIndex) {
        double gravity = 0.04;
        double radius = 6.0;
        double aspect = 2.0;

        // 1. Compute density field values
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double dens = 0.0;
            for (int j = 0; j < PARTICLE_COUNT; j++) {
                double dx = px[i] - px[j];
                double dy = (py[i] - py[j]) * aspect;
                double distSq = dx * dx + dy * dy;
                if (distSq < radius * radius) {
                    dens += 1.0 - (Math.sqrt(distSq) / radius);
                }
            }
            density[i] = dens;
        }

        // 2. Compute pressure-driven fluid forces and velocities
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double fx = 0.0;
            double fy = gravity;

            for (int j = 0; j < PARTICLE_COUNT; j++) {
                if (i == j) continue;

                double dx = px[i] - px[j];
                double dy = (py[i] - py[j]) * aspect;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < radius && dist > 0.01) {
                    // Push away from high density regions
                    double force = 0.15 * (density[i] + density[j]) * (1.0 - (dist / radius));
                    fx += (dx / dist) * force;
                    fy += (dy / dist) * force / aspect;
                }
            }

            // Apply forces
            vx[i] += fx;
            vy[i] += fy;

            // Apply drag/viscosity friction
            vx[i] *= 0.96;
            vy[i] *= 0.96;

            // Update positions
            px[i] += vx[i];
            py[i] += vy[i];

            // Bouncing physics checks
            if (px[i] < 2) {
                px[i] = 2;
                vx[i] = -vx[i] * 0.7;
            } else if (px[i] >= width - 2) {
                px[i] = width - 3;
                vx[i] = -vx[i] * 0.7;
            }

            if (py[i] < 2) {
                py[i] = 2;
                vy[i] = -vy[i] * 0.7;
            } else if (py[i] >= height - 2) {
                py[i] = height - 3;
                vy[i] = -vy[i] * 0.7;
                vx[i] *= 0.85; // Floor friction rolling
            }
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // Background tech ocean grids
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                canvas.writeCell(x, y, ' ', 0, 0x020208);
            }
        }

        // Draw flowing particles with colored visual density glow
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            int sx = (int) Math.round(px[i]);
            int sy = (int) Math.round(py[i]);

            if (sx >= 0 && sx < width && sy >= 0 && sy < height) {
                // High density = Bright Cyan, Low density = Purple
                double dens = density[i];
                int color;
                if (dens > 4.5) {
                    color = 0x22D3EE; // Intense Cyan
                } else if (dens > 2.5) {
                    color = 0x06B6D4; // Cyan-teal
                } else {
                    color = 0x8B5CF6; // Violet
                }

                char glyph = (dens > 4.0) ? '█' : (dens > 2.0) ? '▓' : '▒';
                canvas.writeCell(sx, sy, glyph, color, 0x020208);
            }
        }
    }

    @Override
    public String getName() {
        return "💧 Smoothed Fluid Dynamics Simulation";
    }
}
