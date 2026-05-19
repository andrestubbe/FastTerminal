package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 3D Warp Starfield simulation projecting coordinate streaks with motion blur.
 */
public class WarpStarfieldEffect implements DemosceneEffect {

    private int width;
    private int height;
    
    private static final int NUM_STARS = 150;
    private double[] starX = new double[NUM_STARS];
    private double[] starY = new double[NUM_STARS];
    private double[] starZ = new double[NUM_STARS];
    
    private static final double MAX_Z = 40.0;
    private static final double SPEED = 0.6;
    private static final double FOV = 25.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        for (int i = 0; i < NUM_STARS; i++) {
            resetStar(i);
            // Randomly scatter depth initially
            starZ[i] = Math.random() * MAX_Z;
        }
    }

    private void resetStar(int i) {
        starX[i] = (Math.random() - 0.5) * width * 3.0;
        starY[i] = (Math.random() - 0.5) * height * 3.0;
        starZ[i] = MAX_Z;
    }

    @Override
    public void update(long frameIndex) {
        for (int i = 0; i < NUM_STARS; i++) {
            starZ[i] -= SPEED;

            if (starZ[i] <= 0.2) {
                resetStar(i);
            }
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // Deep space background fill
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                canvas.writeCell(x, y, ' ', 0x000000, 0x030305);
            }
        }

        double centerX = width / 2.0;
        double centerY = height / 2.0;

        for (int i = 0; i < NUM_STARS; i++) {
            double z = starZ[i];
            double prevZ = z + SPEED;

            // Project current coordinate
            int screenX = (int) (centerX + (starX[i] / z) * FOV);
            int screenY = (int) (centerY + (starY[i] / z) * (FOV * 0.5)); // Correct aspect ratio for terminal cells

            // Project previous coordinate to draw dynamic speed warp trails (motion blur)
            int prevScreenX = (int) (centerX + (starX[i] / prevZ) * FOV);
            int prevScreenY = (int) (centerY + (starY[i] / prevZ) * (FOV * 0.5));

            // Compute star brightness and characters based on distance (depth perception)
            double normZ = 1.0 - (z / MAX_Z);
            
            int brightness = (int) (normZ * 200.0) + 55; // 55 (far/dark) to 255 (close/bright)
            int starColor = (brightness << 16) | (brightness << 8) | brightness;

            char trailChar = '·';
            char leadChar = '·';

            if (normZ > 0.75) {
                leadChar = '█'; // Very close: Solid block
                trailChar = '*';
            } else if (normZ > 0.45) {
                leadChar = '*'; // Medium close: Star glyph
                trailChar = '·';
            } else if (normZ > 0.2) {
                leadChar = '·'; // Far away: Simple dot
                trailChar = '·';
            } else {
                leadChar = ' '; // Farthest stars: Faint single dot (no streak)
                trailChar = '·';
            }

            // Draw streak trail with depth variables
            drawLine(canvas, prevScreenX, prevScreenY, screenX, screenY, starColor, trailChar, leadChar);
        }
    }

    /**
     * Standard Bresenham line algorithm to render motion streaks with dynamic depth glyphs.
     */
    private void drawLine(FastTerminalScene canvas, int x0, int y0, int x1, int y1, int color, char trailChar, char leadChar) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int cx = x0;
        int cy = y0;

        while (true) {
            if (cx >= 0 && cx < width && cy >= 0 && cy < height) {
                if (trailChar != ' ') {
                    canvas.writeCell(cx, cy, trailChar, color, 0x030305);
                }
            }

            if (cx == x1 && cy == y1) {
                // Draw leading star edge
                if (cx >= 0 && cx < width && cy >= 0 && cy < height) {
                    if (leadChar != ' ') {
                        // Leading character is slightly brighter for extra contrast
                        int r = Math.min(255, ((color >> 16) & 0xFF) + 30);
                        int g = Math.min(255, ((color >> 8) & 0xFF) + 30);
                        int b = Math.min(255, (color & 0xFF) + 30);
                        int brightColor = (r << 16) | (g << 8) | b;
                        canvas.writeCell(cx, cy, leadChar, brightColor, 0x030305);
                    }
                }
                break;
            }

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                cx += sx;
            }
            if (e2 < dx) {
                err += dx;
                cy += sy;
            }
        }
    }

    @Override
    public String getName() {
        return "🌌 3D WARP STARFIELD";
    }
}
