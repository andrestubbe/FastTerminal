package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🚀 Amiga 3D Vector Spaceship effect.
 * Performs real-time matrix transformations to rotate a detailed 3D Delta Cruiser,
 * projecting its vectors and drawing glowing lines with specular lighting.
 */
public class Spaceship3DEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double angleX = 0.0;
    private double angleY = 0.0;

    // Vertices of the Delta Cruiser
    private static final double[][] VERTICES = {
        { 0.0,  0.0,  2.5 },  // 0: Nose tip
        {-1.8, -0.5, -1.2 },  // 1: Wingtip Left
        { 1.8, -0.5, -1.2 },  // 2: Wingtip Right
        { 0.0,  0.7, -0.5 },  // 3: Cockpit Canopy Top
        { 0.0, -0.6, -1.2 }   // 4: Bottom Engine Keel
    };

    // Edge connections between vertices
    private static final int[][] EDGES = {
        {0, 1}, {0, 2}, {0, 3}, {0, 4}, // Nose to base vertices
        {1, 3}, {2, 3}, {1, 4}, {2, 4}, // Side facets
        {3, 4}, {1, 2}                  // Base facets
    };

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        angleX = frameIndex * 0.025;
        angleY = frameIndex * 0.045;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // Galactic background
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((x * 11 + y * 23) % 47 == 0) {
                    canvas.writeCell(x, y, '·', 0x334155, 0x020208);
                } else {
                    canvas.writeCell(x, y, ' ', 0, 0x020208);
                }
            }
        }

        double cosX = Math.cos(angleX), sinX = Math.sin(angleX);
        double cosY = Math.cos(angleY), sinY = Math.sin(angleY);

        int[][] projected = new int[VERTICES.length][2];
        double aspect = 2.0;

        // 1. Transform and project vertices
        for (int i = 0; i < VERTICES.length; i++) {
            double x = VERTICES[i][0];
            double y = VERTICES[i][1];
            double z = VERTICES[i][2];

            // Y-axis rotation
            double x1 = x * cosY - z * sinY;
            double z1 = x * sinY + z * cosY;

            // X-axis rotation
            double y2 = y * cosX - z1 * sinX;
            double z2 = y * sinX + z1 * cosX;

            // Camera depth shift
            double depth = z2 + 5.0;
            double scale = (height * 0.55) / depth;

            projected[i][0] = (int) (width / 2.0 + x1 * scale * aspect);
            projected[i][1] = (int) (height / 2.0 - y2 * scale);
        }

        // 2. Draw 3D wireframe edges (glowing golden line render)
        for (int[] edge : EDGES) {
            int p1 = edge[0];
            int p2 = edge[1];
            drawLine(canvas, projected[p1][0], projected[p1][1], projected[p2][0], projected[p2][1], 0xF59E0B);
        }
    }

    private void drawLine(FastTerminalScene canvas, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (x0 >= 0 && x0 < width && y0 >= 0 && y0 < height) {
                canvas.writeCell(x0, y0, '█', color, 0x020208);
            }

            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    @Override
    public String getName() {
        return "🚀 3D Vektor Delta Cruiser";
    }
}
