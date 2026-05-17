package fastterminal;

import java.util.Random;

/**
 * High-Performance real-time 3D Wireframe Cube Demo.
 * Features tumbling 3D matrix rotations, perspective depth scaling,
 * Bresenham's line blitting, and a twinkling starfield backdrop at 120 FPS.
 */
public class CubeDemo {

    // 3D Cube Vertices (centered at 0, 0, 0)
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

    // 12 Edges connecting the 8 vertices
    private static final int[][] EDGES = {
        {0, 1}, {1, 2}, {2, 3}, {3, 0}, // Back face
        {4, 5}, {5, 6}, {6, 7}, {7, 4}, // Front face
        {0, 4}, {1, 5}, {2, 6}, {3, 7}  // Cross connections
    };

    // Starfield representation
    private static final int STAR_COUNT = 60;
    private static final double[] STAR_X = new double[STAR_COUNT];
    private static final double[] STAR_Y = new double[STAR_COUNT];
    private static final double[] STAR_SPEED = new double[STAR_COUNT];
    private static final int[] STAR_COLOR = new int[STAR_COUNT];

    static {
        Random rand = new Random();
        for (int i = 0; i < STAR_COUNT; i++) {
            STAR_X[i] = rand.nextDouble();
            STAR_Y[i] = rand.nextDouble();
            STAR_SPEED[i] = 0.001 + rand.nextDouble() * 0.003;
            // Warm white, bright white, slate gray stars
            int choice = rand.nextInt(3);
            if (choice == 0) STAR_COLOR[i] = 0x94A3B8; // Slate gray (far away)
            else if (choice == 1) STAR_COLOR[i] = 0xCBD5E1; // Silver
            else STAR_COLOR[i] = 0xF8FAFC; // Glowing white
        }
    }

    public static void main(String[] args) {
        System.out.println("Initializing FastTerminal 3D Rotating Cube Demo...");

        // Register JVM Shutdown Hook to safely restore the console on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print("\033[?1049l\033[?25h\033[0m");
            System.out.flush();
        }));

        // Enter Alternate Screen Buffer, Hide Cursor
        System.out.print("\033[?1049h\033[?25l");
        System.out.flush();

        int cols = 80;
        int rows = 30;

        // Try to query starting size natively
        try {
            int[] size = FastTerminal.getTerminalSize();
            if (size != null && size[0] > 0 && size[1] > 0) {
                cols = size[0];
                rows = size[1];
            }
        } catch (Throwable ignored) {}

        TerminalRenderer renderer = null;
        TerminalScene canvas = null;

        double angleX = 0.0;
        double angleY = 0.0;
        double angleZ = 0.0;
        double colorPhase = 0.0;
        long frameTimeMs = 1000 / 120; // 120 FPS target

        while (true) {
            long startTime = System.currentTimeMillis();

            // 1. DYNAMIC RESIZE DETECTION via Win32 JNI
            int currentCols = cols;
            int currentRows = rows;
            try {
                int[] size = FastTerminal.getTerminalSize();
                if (size != null && size[0] > 0 && size[1] > 0) {
                    currentCols = size[0];
                    currentRows = size[1];
                }
            } catch (Throwable ignored) {}

            // Recreate viewport scene if resized
            if (renderer == null || canvas == null || currentCols != cols || currentRows != rows) {
                cols = currentCols;
                rows = currentRows;
                renderer = new TerminalRenderer(cols, rows);
                canvas = new TerminalScene(0, 0, cols, rows);
                renderer.addScene(canvas);
            }

            canvas.clear();

            // Increment rotation angles & color phase
            angleX += 0.025;
            angleY += 0.035;
            angleZ += 0.015;
            colorPhase += 0.04;

            // 2. FILL CANVAS BACKGROUND WITH OBSIDIAN BLUE
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    canvas.writeCell(c, r, ' ', 0x000000, 0x05070A); // Space black background
                }
            }

            // 3. DRAW AND DRIFT STARFIELD BACKDROP
            for (int i = 0; i < STAR_COUNT; i++) {
                STAR_X[i] -= STAR_SPEED[i]; // Move left
                if (STAR_X[i] < 0) {
                    STAR_X[i] = 1.0;
                }
                int sx = (int) (STAR_X[i] * cols);
                int sy = (int) (STAR_Y[i] * rows);

                if (sx >= 0 && sx < cols && sy >= 0 && sy < rows) {
                    // Draw a subtle twinkling dot
                    int cp = '.';
                    if (STAR_SPEED[i] > 0.003) cp = '*';
                    canvas.writeCell(sx, sy, cp, STAR_COLOR[i], 0x05070A);
                }
            }

            // 3. TRANSFORM AND ROTATE VERTICES
            int[][] projected = new int[VERTICES.length][2];
            double cameraDistance = 3.2;

            for (int i = 0; i < VERTICES.length; i++) {
                double x = VERTICES[i][0];
                double y = VERTICES[i][1];
                double z = VERTICES[i][2];

                // Rotate around X axis
                double y1 = y * Math.cos(angleX) - z * Math.sin(angleX);
                double z1 = y * Math.sin(angleX) + z * Math.cos(angleX);

                // Rotate around Y axis
                double x2 = x * Math.cos(angleY) + z1 * Math.sin(angleY);
                double z2 = -x * Math.sin(angleY) + z1 * Math.cos(angleY);

                // Rotate around Z axis
                double x3 = x2 * Math.cos(angleZ) - y1 * Math.sin(angleZ);
                double y3 = x2 * Math.sin(angleZ) + y1 * Math.cos(angleZ);

                // Perspective projection mapping with terminal cell aspect correction (2.1 horizontal factor)
                double scale = (rows * 0.5) / (cameraDistance + z2);
                projected[i][0] = (int) (cols / 2.0 + x3 * scale * 2.1);
                projected[i][1] = (int) (rows / 2.0 + y3 * scale);
            }

            // 4. DRAW CUBE EDGES (Bresenham Line Blitting with neon color cycling)
            for (int i = 0; i < EDGES.length; i++) {
                int p0 = EDGES[i][0];
                int p1 = EDGES[i][1];

                int x0 = projected[p0][0];
                int y0 = projected[p0][1];
                int x1 = projected[p1][0];
                int y1 = projected[p1][1];

                // Dynamically cycle colors of each edge
                int edgeColor = getNeonColor(colorPhase + (double) i * 0.5);
                drawLine(canvas, x0, y0, x1, y1, edgeColor, 0x05070A);
            }

            // 5. DRAW VERTEXT HIGHLIGHTS (Small nodes)
            for (int i = 0; i < VERTICES.length; i++) {
                int vx = projected[i][0];
                int vy = projected[i][1];
                if (vx >= 0 && vx < cols && vy >= 0 && vy < rows) {
                    canvas.writeCell(vx, vy, 'o', 0xFFFFFF, 0x05070A); // Bright node dots
                }
            }



            // Blit standard composite buffers to screen
            canvas.setDirty(true);
            renderer.render();

            // 120 FPS sleep throttle
            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = frameTimeMs - elapsed;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private static int getNeonColor(double huePhase) {
        int r = (int) (Math.sin(huePhase) * 110 + 145);
        int g = (int) (Math.sin(huePhase + 2.0 * Math.PI / 3.0) * 110 + 145);
        int b = (int) (Math.sin(huePhase + 4.0 * Math.PI / 3.0) * 110 + 145);
        return (r << 16) | (g << 8) | b;
    }

    // High-Performance Bresenham Line Drawing Algorithm
    private static void drawLine(TerminalScene canvas, int x0, int y0, int x1, int y1, int fgColor, int bgColor) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (x0 >= 0 && x0 < canvas.getWidth() && y0 >= 0 && y0 < canvas.getHeight()) {
                // Renders using a solid block to represent edge lines elegantly
                canvas.writeCell(x0, y0, '█', fgColor, bgColor);
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
}
