package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🧬 Organic Fluid Metaballs merger effect.
 * Dynamically computes a scalar distance field for floating biological/fluid points.
 */
public class MetaballsEffect implements DemosceneEffect {

    private int width;
    private int height;
    private float time = 0f;

    // Ball positions and velocities
    private float bx1, by1, vx1, vy1;
    private float bx2, by2, vx2, vy2;
    private float bx3, by3, vx3, vy3;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        // Initialize positions
        bx1 = width * 0.25f;  by1 = height * 0.5f;
        bx2 = width * 0.5f;   by2 = height * 0.25f;
        bx3 = width * 0.75f;  by3 = height * 0.75f;

        // Initialize velocities (in columns/rows per tick)
        vx1 = 0.6f;  vy1 = 0.3f;
        vx2 = -0.4f; vy2 = 0.5f;
        vx3 = 0.5f;  vy3 = -0.4f;
    }

    @Override
    public void update(long frameIndex) {
        time = frameIndex * 0.05f;

        // 1. Move and bounce Ball 1
        bx1 += vx1; by1 += vy1;
        if (bx1 < 2 || bx1 >= width - 2) vx1 = -vx1;
        if (by1 < 2 || by1 >= height - 2) vy1 = -vy1;

        // 2. Move and bounce Ball 2
        bx2 += vx2; by2 += vy2;
        if (bx2 < 2 || bx2 >= width - 2) vx2 = -vx2;
        if (by2 < 2 || by2 >= height - 2) vy2 = -vy2;

        // 3. Move and bounce Ball 3
        bx3 += vx3; by3 += vy3;
        if (bx3 < 2 || bx3 >= width - 2) vx3 = -vx3;
        if (by3 < 2 || by3 >= height - 2) vy3 = -vy3;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        // Metaball radii parameters
        float r1 = 120.0f;
        float r2 = 90.0f;
        float r3 = 100.0f;

        for (int y = 0; y < height; y++) {
            float yPos = y * 2.0f; // character aspect ratio compensation

            for (int x = 0; x < width; x++) {
                float xPos = (float) x;

                // Calculate squared distances to avoid Math.sqrt
                float dx1 = xPos - bx1; float dy1 = yPos - (by1 * 2.0f);
                float distSq1 = dx1 * dx1 + dy1 * dy1;

                float dx2 = xPos - bx2; float dy2 = yPos - (by2 * 2.0f);
                float distSq2 = dx2 * dx2 + dy2 * dy2;

                float dx3 = xPos - bx3; float dy3 = yPos - (by3 * 2.0f);
                float distSq3 = dx3 * dx3 + dy3 * dy3;

                // Add up metaball inverse distances (intensities)
                float sum = 0f;
                sum += r1 / (distSq1 + 1.0f);
                sum += r2 / (distSq2 + 1.0f);
                sum += r3 / (distSq3 + 1.0f);

                // Threshold gate for merging
                if (sum > 0.85f) {
                    // Hot glowing neon plasma core
                    int r = (int) (230 + 25 * Math.sin(time + sum));
                    int g = (int) (30 + 80 * Math.cos(time * 0.7f + xPos * 0.05f));
                    int b = (int) (180 + 75 * Math.sin(time * 0.5f));
                    int color = (r << 16) | (g << 8) | b;
                    
                    // High core gets lighter shade
                    char glyph = (sum > 1.3f) ? '█' : '▓';
                    canvas.writeCell(x, y, glyph, color, 0x05020F);
                } else if (sum > 0.45f) {
                    // Soft glowing aura surrounding the metaballs
                    int r = (int) (50 + 20 * Math.sin(time));
                    int g = 10;
                    int b = (int) (100 + 40 * Math.sin(time * 0.8f + sum));
                    int color = (r << 16) | (g << 8) | b;
                    canvas.writeCell(x, y, '▒', color, 0x02000A);
                } else {
                    // Deep void
                    canvas.writeCell(x, y, ' ', 0, 0x000002);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🧬 Organic Fluid Metaballs";
    }
}
