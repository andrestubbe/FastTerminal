package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;
import java.util.Random;

/**
 * 🧊 3D Wireframe Tumbling Cube Effect with dynamic starfield background.
 */
public class CubeEffect implements DemosceneEffect {

    private int width;
    private int height;

    private static final double[][] VERTICES = {
        {-1.0, -1.0, -1.0},
        { 1.0, -1.0, -1.0},
        { 1.0,  1.0, -1.0},
        {-1.0,  1.0, -1.0},
        {-1.0, -1.0,  1.0},
        { 1.0, -1.0,  1.0},
        { 1.0,  1.0,  1.0},
        {-1.0,  1.0,  1.0}
    };

    private static final int[][] EDGES = {
        {0, 1}, {1, 2}, {2, 3}, {3, 0}, // Back face
        {4, 5}, {5, 6}, {6, 7}, {7, 4}, // Front face
        {0, 4}, {1, 5}, {2, 6}, {3, 7}  // Cross connections
    };

    private static final int STAR_COUNT = 60;
    private double[] starX;
    private double[] starY;
    private double[] starSpeed;
    private int[] starColor;

    private double angleX = 0.0;
    private double angleY = 0.0;
    private double angleZ = 0.0;
    private double colorPhase = 0.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        Random rand = new Random();
        starX = new double[STAR_COUNT];
        starY = new double[STAR_COUNT];
        starSpeed = new double[STAR_COUNT];
        starColor = new int[STAR_COUNT];

        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = rand.nextDouble();
            starY[i] = rand.nextDouble();
            starSpeed[i] = 0.001 + rand.nextDouble() * 0.003;
            int choice = rand.nextInt(3);
            if (choice == 0) starColor[i] = 0x94A3B8; // Slate gray
            else if (choice == 1) starColor[i] = 0xCBD5E1; // Silver
            else starColor[i] = 0xF8FAFC; // White
        }
    }

    @Override
    public void update(long frameIndex) {
        // Tumbling rotations matched to original demo
        angleX += 0.025 / 3.0;
        angleY += 0.035 / 3.0;
        angleZ += 0.015 / 3.0;
        colorPhase += 0.04 / 3.0;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();
        // 1. Draw and drift background starfield
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] -= starSpeed[i];
            if (starX[i] < 0) starX[i] = 1.0;
            
            int sx = (int) (starX[i] * width);
            int sy = (int) (starY[i] * height);

            if (sx >= 0 && sx < width && sy >= 0 && sy < height) {
                int cp = '.';
                if (starSpeed[i] > 0.003) cp = '*';
                canvas.writeCell(sx, sy, cp, starColor[i], 0x05070A);
            }
        }

        // Fill rest with obsidian base
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (canvas.getCodepointBuffer()[y * width + x] == 0) {
                    canvas.writeCell(x, y, ' ', 0, 0x05070A);
                }
            }
        }

        // 2. Transform vertices with aspect correction (2.1 horizontal multiplier)
        int[][] projected = new int[VERTICES.length][2];
        double cameraDistance = 3.2;

        for (int i = 0; i < VERTICES.length; i++) {
            double x = VERTICES[i][0];
            double y = VERTICES[i][1];
            double z = VERTICES[i][2];

            // Rotation
            double y1 = y * Math.cos(angleX) - z * Math.sin(angleX);
            double z1 = y * Math.sin(angleX) + z * Math.cos(angleX);

            double x2 = x * Math.cos(angleY) + z1 * Math.sin(angleY);
            double z2 = -x * Math.sin(angleY) + z1 * Math.cos(angleY);

            double x3 = x2 * Math.cos(angleZ) - y1 * Math.sin(angleZ);
            double y3 = x2 * Math.sin(angleZ) + y1 * Math.cos(angleZ);

            double scale = (height * 0.5) / (cameraDistance + z2);
            projected[i][0] = (int) (width / 2.0 + x3 * scale * 2.1);
            projected[i][1] = (int) (height / 2.0 + y3 * scale);
        }

        // 3. Draw edge lines using Bresenham algorithm
        for (int i = 0; i < EDGES.length; i++) {
            int p0 = EDGES[i][0];
            int p1 = EDGES[i][1];

            int x0 = projected[p0][0];
            int y0 = projected[p0][1];
            int x1 = projected[p1][0];
            int y1 = projected[p1][1];

            int edgeColor = getNeonColor(colorPhase + (double) i * 0.5);
            drawLine(canvas, x0, y0, x1, y1, edgeColor, 0x05070A);
        }

        // 4. Highlight vertices
        for (int i = 0; i < VERTICES.length; i++) {
            int vx = projected[i][0];
            int vy = projected[i][1];
            if (vx >= 0 && vx < width && vy >= 0 && vy < height) {
                canvas.writeCell(vx, vy, 'o', 0xFFFFFF, 0x05070A);
            }
        }
    }

    private int getNeonColor(double huePhase) {
        int r = (int) (Math.sin(huePhase) * 110 + 145);
        int g = (int) (Math.sin(huePhase + 2.0 * Math.PI / 3.0) * 110 + 145);
        int b = (int) (Math.sin(huePhase + 4.0 * Math.PI / 3.0) * 110 + 145);
        return (r << 16) | (g << 8) | b;
    }

    private void drawLine(FastTerminalScene canvas, int x0, int y0, int x1, int y1, int fg, int bg) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (x0 >= 0 && x0 < width && y0 >= 0 && y0 < height) {
                canvas.writeCell(x0, y0, '█', fg, bg);
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
        return "🧊 3D Wireframe Tumbling Cube";
    }
}
