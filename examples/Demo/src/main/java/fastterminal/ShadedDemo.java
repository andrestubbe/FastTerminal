package fastterminal;

import java.util.Random;

/**
 * Premium 3D Solid Shaded Cube Demo.
 * Features real-time scanline triangle rasterization, custom back-face culling,
 * Lambertian diffuse flat shading with a dynamic orbiting 3D light source,
 * and a drifting starfield background running at a locked 120 FPS.
 */
public class ShadedDemo {

    // 3D Cube Vertices (centered at 0, 0, 0)
    private static final double[][] VERTICES = {
        {-1.0, -1.0, -1.0}, // 0
        { 1.0, -1.0, -1.0}, // 1
        { 1.0,  1.0, -1.0}, // 2
        {-1.0,  1.0, -1.0}, // 3
        {-1.0, -1.0,  1.0}, // 4
        { 1.0, -1.0,  1.0}, // 5
        { 1.0,  1.0,  1.0}, // 6
        {-1.0,  1.0,  1.0}  // 7
    };

    // 6 Cube Faces (each defined by 4 vertex indices in counter-clockwise order)
    private static final int[][] FACES = {
        {4, 5, 6, 7}, // Front face (normal: 0, 0, 1)
        {1, 0, 3, 2}, // Back face  (normal: 0, 0, -1)
        {3, 2, 6, 7}, // Top face   (normal: 0, 1, 0)
        {0, 1, 5, 4}, // Bottom face(normal: 0, -1, 0)
        {1, 5, 6, 2}, // Right face (normal: 1, 0, 0)
        {4, 0, 3, 7}  // Left face  (normal: -1, 0, 0)
    };

    // Original normals of the 6 faces
    private static final double[][] NORMALS = {
        { 0.0,  0.0,  1.0}, // Front
        { 0.0,  0.0, -1.0}, // Back
        { 0.0,  1.0,  0.0}, // Top
        { 0.0, -1.0,  0.0}, // Bottom
        { 1.0,  0.0,  0.0}, // Right
        {-1.0,  0.0,  0.0}  // Left
    };

    // Base colors for each face of the cube (warm violet, royal blue, cool teal, electric crimson, magenta, emerald)
    private static final int[] FACE_COLORS = {
        0x7C3AED, // Front (Amethyst Purple)
        0x1D4ED8, // Back (Royal Blue)
        0x0D9488, // Top (Teal)
        0xE11D48, // Bottom (Crimson)
        0xDB2777, // Right (Magenta)
        0x059669  // Left (Emerald Green)
    };

    // Starfield representation
    private static final int STAR_COUNT = 50;
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
            int choice = rand.nextInt(3);
            if (choice == 0) STAR_COLOR[i] = 0x64748B; // Faint gray
            else if (choice == 1) STAR_COLOR[i] = 0x94A3B8; // Slate
            else STAR_COLOR[i] = 0xE2E8F0; // Bright star
        }
    }

    public static void main(String[] args) {
        System.out.println("Initializing FastTerminal 3D Shaded Cube Demo...");

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
        double lightPhase = 0.0;
        long frameTimeMs = 1000 / 120; // 120 FPS target

        while (true) {
            long startTime = System.currentTimeMillis();

            // 1. DYNAMIC RESIZE DETECTION via JNI
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

            // Slow down angles for majestic rotations (1/3 of original speed)
            angleX += 0.025 / 3.0;
            angleY += 0.035 / 3.0;
            angleZ += 0.015 / 3.0;
            lightPhase += 0.01; // Orbiting light speed

            // 2. FILL CANVAS BACKGROUND WITH OBSIDIAN SPACE BLACK
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    canvas.writeCell(c, r, ' ', 0x000000, 0x05070A);
                }
            }

            // 3. DRAW AND DRIFT STARFIELD BACKDROP
            for (int i = 0; i < STAR_COUNT; i++) {
                STAR_X[i] -= STAR_SPEED[i];
                if (STAR_X[i] < 0) {
                    STAR_X[i] = 1.0;
                }
                int sx = (int) (STAR_X[i] * cols);
                int sy = (int) (STAR_Y[i] * rows);

                if (sx >= 0 && sx < cols && sy >= 0 && sy < rows) {
                    int cp = (STAR_SPEED[i] > 0.003) ? '*' : '.';
                    canvas.writeCell(sx, sy, cp, STAR_COLOR[i], 0x05070A);
                }
            }

            // 4. DYNAMIC ORBITING LIGHT SOURCE
            double lx = Math.cos(lightPhase);
            double ly = -0.5;
            double lz = Math.sin(lightPhase);
            // Normalize light vector
            double len = Math.sqrt(lx * lx + ly * ly + lz * lz);
            lx /= len;
            ly /= len;
            lz /= len;

            // 5. TRANSFORM AND ROTATE VERTICES
            int[][] projected = new int[VERTICES.length][2];
            double[] rotatedZ = new double[VERTICES.length];
            double cameraDistance = 3.2;

            for (int i = 0; i < VERTICES.length; i++) {
                double x = VERTICES[i][0];
                double y = VERTICES[i][1];
                double z = VERTICES[i][2];

                // Pitch (X rotation)
                double y1 = y * Math.cos(angleX) - z * Math.sin(angleX);
                double z1 = y * Math.sin(angleX) + z * Math.cos(angleX);

                // Yaw (Y rotation)
                double x2 = x * Math.cos(angleY) + z1 * Math.sin(angleY);
                double z2 = -x * Math.sin(angleY) + z1 * Math.cos(angleY);

                // Roll (Z rotation)
                double x3 = x2 * Math.cos(angleZ) - y1 * Math.sin(angleZ);
                double y3 = x2 * Math.sin(angleZ) + y1 * Math.cos(angleZ);

                rotatedZ[i] = z2;

                // Perspective projection with terminal cell aspect ratio correction (2.1x width scaling)
                double scale = (rows * 0.5) / (cameraDistance + z2);
                projected[i][0] = (int) (cols / 2.0 + x3 * scale * 2.1);
                projected[i][1] = (int) (rows / 2.0 + y3 * scale);
            }

            // 6. CALCULATE, CULL, SHADE, AND RENDER FACE POLYGONS
            // We use simple painter's sorting by z-depth to prevent rendering overlaps (Z-Buffering)
            Integer[] faceOrder = {0, 1, 2, 3, 4, 5};
            double[] faceAverageZ = new double[6];
            for (int f = 0; f < 6; f++) {
                double avgZ = 0.0;
                for (int vIdx : FACES[f]) {
                    avgZ += rotatedZ[vIdx];
                }
                faceAverageZ[f] = avgZ / 4.0;
            }

            // Sort faces from furthest to nearest (descending order of Z depth)
            java.util.Arrays.sort(faceOrder, (a, b) -> Double.compare(faceAverageZ[b], faceAverageZ[a]));

            for (int f : faceOrder) {
                // Calculate rotated normal of this face
                double nx = NORMALS[f][0];
                double ny = NORMALS[f][1];
                double nz = NORMALS[f][2];

                // Pitch (X rotation)
                double ny1 = ny * Math.cos(angleX) - nz * Math.sin(angleX);
                double nz1 = ny * Math.sin(angleX) + nz * Math.cos(angleX);

                // Yaw (Y rotation)
                double nx2 = nx * Math.cos(angleY) + nz1 * Math.sin(angleY);
                double nz2 = -nx * Math.sin(angleY) + nz1 * Math.cos(angleY);

                // Roll (Z rotation)
                double nx3 = nx2 * Math.cos(angleZ) - ny1 * Math.sin(angleZ);
                double ny3 = nx2 * Math.sin(angleZ) + ny1 * Math.cos(angleZ);

                // Back-face Culling: If normal points away from observer (camera faces Z-), do not draw!
                if (nz2 < 0) continue;

                // Lambertian Diffuse shading: Dot product of normal and light source direction
                double dot = nx3 * lx + ny3 * ly + nz2 * lz;
                double ambient = 0.18; // Base shadow brightness
                double shade = Math.max(0.0, dot); // Clamped lighting
                double intensity = ambient + (1.0 - ambient) * shade;

                // Compute shaded face color (24-bit True Color)
                int baseColor = FACE_COLORS[f];
                int r = (int) (((baseColor >> 16) & 0xFF) * intensity);
                int g = (int) (((baseColor >> 8) & 0xFF) * intensity);
                int b = (int) ((baseColor & 0xFF) * intensity);
                int shadedColor = (r << 16) | (g << 8) | b;

                // Draw the Quad Face by splitting it into two triangles
                int[] v = FACES[f];
                fillTriangle(canvas, projected[v[0]][0], projected[v[0]][1],
                                    projected[v[1]][0], projected[v[1]][1],
                                    projected[v[2]][0], projected[v[2]][1], shadedColor);

                fillTriangle(canvas, projected[v[0]][0], projected[v[0]][1],
                                    projected[v[2]][0], projected[v[2]][1],
                                    projected[v[3]][0], projected[v[3]][1], shadedColor);
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

    // High-Performance Scanline Triangle Rasterizer filling cells with '█' blocks
    private static void fillTriangle(TerminalScene canvas, int x0, int y0, int x1, int y1, int x2, int y2, int color) {
        // Sort vertices by Y coordinate (y0 <= y1 <= y2)
        if (y0 > y1) { int t = y0; y0 = y1; y1 = t; t = x0; x0 = x1; x1 = t; }
        if (y0 > y2) { int t = y0; y0 = y2; y2 = t; t = x0; x0 = x2; x2 = t; }
        if (y1 > y2) { int t = y1; y1 = y2; y2 = t; t = x1; x1 = x2; x2 = t; }

        int totalHeight = y2 - y0;
        if (totalHeight == 0) return;

        for (int y = y0; y <= y2; y++) {
            if (y < 0 || y >= canvas.getHeight()) continue;

            boolean secondHalf = y > y1 || y1 == y0;
            int segmentHeight = secondHalf ? y2 - y1 : y1 - y0;
            if (segmentHeight == 0) continue;

            double alpha = (double) (y - y0) / totalHeight;
            double beta  = (double) (secondHalf ? (y - y1) : (y - y0)) / segmentHeight;

            int ax = x0 + (int) ((x2 - x0) * alpha);
            int bx = secondHalf ? x1 + (int) ((x2 - x1) * beta) : x0 + (int) ((x1 - x0) * beta);

            if (ax > bx) { int t = ax; ax = bx; bx = t; }

            for (int x = ax; x <= bx; x++) {
                if (x >= 0 && x < canvas.getWidth()) {
                    canvas.writeCell(x, y, '█', color, 0x05070A);
                }
            }
        }
    }
}
