package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;
import java.util.Random;

/**
 * 🧬 Organic Fluid Metaballs merger effect.
 * Dynamically computes a scalar distance field for 8 floating biological/fluid points
 * and merges them together inside a single premium neon color scheme.
 */
public class MetaballsEffect implements DemosceneEffect {

    private int width;
    private int height;

    private static final int BALL_COUNT = 8;
    private float[] bx = new float[BALL_COUNT];
    private float[] by = new float[BALL_COUNT];
    private float[] vx = new float[BALL_COUNT];
    private float[] vy = new float[BALL_COUNT];
    private float[] radius = new float[BALL_COUNT];

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        Random rand = new Random(42); // Seeded for deterministic beautiful initialization
        for (int i = 0; i < BALL_COUNT; i++) {
            bx[i] = width * 0.15f + rand.nextFloat() * width * 0.7f;
            by[i] = height * 0.15f + rand.nextFloat() * height * 0.7f;
            vx[i] = (rand.nextFloat() - 0.5f) * 1.6f;
            vy[i] = (rand.nextFloat() - 0.5f) * 1.6f;
            // Radii parameters matching screen aspect limits
            radius[i] = 60.0f + rand.nextFloat() * 55.0f;
        }
    }

    @Override
    public void update(long frameIndex) {
        for (int i = 0; i < BALL_COUNT; i++) {
            bx[i] += vx[i];
            by[i] += vy[i];
            
            // Boundary bounce physics gates
            if (bx[i] < 4 || bx[i] >= width - 4) vx[i] = -vx[i];
            if (by[i] < 4 || by[i] >= height - 4) vy[i] = -vy[i];
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        float aspect = 2.0f; // character cell aspect ratio compensation

        for (int y = 0; y < height; y++) {
            float yPos = y * aspect;

            for (int x = 0; x < width; x++) {
                float xPos = (float) x;

                // Sum metaball fields
                float sum = 0f;
                for (int i = 0; i < BALL_COUNT; i++) {
                    float dx = xPos - bx[i];
                    float dy = yPos - (by[i] * aspect);
                    float distSq = dx * dx + dy * dy;
                    sum += radius[i] / (distSq + 1.0f);
                }

                // Threshold gate for biological merging
                if (sum > 0.85f) {
                    // Premium Neon Pink solid core (0xEC4899)
                    int color = 0xEC4899;
                    char glyph = (sum > 1.3f) ? '█' : '▓';
                    canvas.writeCell(x, y, glyph, color, 0x05010B);
                } else if (sum > 0.45f) {
                    // Soft Deep Violet aura (0x8B5CF6)
                    int color = 0x8B5CF6;
                    canvas.writeCell(x, y, '▒', color, 0x010006);
                } else {
                    // Deep space void
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
