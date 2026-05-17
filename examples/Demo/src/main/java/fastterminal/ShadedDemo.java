package fastterminal;

/**
 * Premium 3D Solid Shaded Studio Demo.
 * Renders a monochromatic solid 3D cube floating over a perspective-vanishing
 * white floor plane, casting a real-time dynamic perspective shadow directly below it.
 * Runs at a locked 120 FPS with JNI resizing.
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
        {4, 5, 6, 7}, // Front face
        {1, 0, 3, 2}, // Back face
        {3, 2, 6, 7}, // Top face
        {0, 1, 5, 4}, // Bottom face
        {1, 5, 6, 2}, // Right face
        {4, 0, 3, 7}  // Left face
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

    // Monochromatic slate/silver base colors for each face of the cube
    private static final int[] FACE_COLORS = {
        0xF1F5F9, // Front (Bright Slate White)
        0xE2E8F0, // Back (Silver)
        0xCBD5E1, // Top (Light Gray)
        0x94A3B8, // Bottom (Slate Gray)
        0x64748B, // Right (Muted Gray)
        0x475569  // Left (Dark Slate)
    };

    public static void main(String[] args) {
        System.out.println("Initializing FastTerminal 3D Shaded Studio Demo...");

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

            // Rotation angles at 1/3 speed
            angleX += 0.025 / 3.0;
            angleY += 0.035 / 3.0;
            angleZ += 0.015 / 3.0;

            // 2. RENDER THE VANISHING SHADED STUDIO BACKGROUND (Sky & Floor)
            int horizon = rows / 2;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (r < horizon) {
                        // Sky: Ambient dark charcoal
                        canvas.writeCell(c, r, ' ', 0x000000, 0x0F172A);
                    } else {
                        // Floor: Grayscale depth gradient vanishing into the horizon
                        double t = (double) (r - horizon) / (rows - horizon);
                        // Fades from deep slate 0x334155 (at horizon) to clean bright silver 0xDFE2E6 (at bottom)
                        int gray = 0x22 + (int) (t * (0xD4 - 0x22));
                        int floorColor = (gray << 16) | (gray << 8) | gray;
                        canvas.writeCell(c, r, ' ', 0x000000, floorColor);
                    }
                }
            }

            // 3. FIXED TOP LIGHT SOURCE (Slightly forward angled)
            double lx = 0.2;
            double ly = 1.0;
            double lz = -0.3;
            double len = Math.sqrt(lx * lx + ly * ly + lz * lz);
            lx /= len;
            ly /= len;
            lz /= len;

            // Camera specifications
            double cameraDistance = 3.2;
            double yTranslation = -0.3; // Cube floats slightly above center
            double floorY = 1.1;       // Fixed 3D plane coordinate for floor

            // 4. ROTATE VERTICES AND PROJECT CUBE AND PROJECTED SHADOW POINTS
            int[][] projectedCube = new int[VERTICES.length][2];
            int[][] projectedShadow = new int[VERTICES.length][2];
            double[] rotatedZ = new double[VERTICES.length];

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

                // Floated 3D Cube Coordinate
                double cubeY = y3 + yTranslation;
                rotatedZ[i] = z2;

                // Perspective projection for Cube Vertices (Aspect-ratio corrected: 2.1x width)
                double scaleCube = (rows * 0.5) / (cameraDistance + z2);
                projectedCube[i][0] = (int) (cols / 2.0 + x3 * scaleCube * 2.1);
                projectedCube[i][1] = (int) (rows / 2.0 + cubeY * scaleCube);

                // Perspective projection for Shadow cast on flat Floor plane (y = floorY)
                double scaleShadow = (rows * 0.5) / (cameraDistance + z2);
                projectedShadow[i][0] = (int) (cols / 2.0 + x3 * scaleShadow * 2.1);
                projectedShadow[i][1] = (int) (rows / 2.0 + floorY * scaleShadow);
            }

            // 5. DRAW CUBE SHADOW FACES ON THE FLOOR FIRST
            // Shadow color is a flat dark charcoal slate overlay
            int shadowColor = 0x1E293B;
            for (int f = 0; f < 6; f++) {
                int[] v = FACES[f];
                fillTriangle(canvas, projectedShadow[v[0]][0], projectedShadow[v[0]][1],
                                    projectedShadow[v[1]][0], projectedShadow[v[1]][1],
                                    projectedShadow[v[2]][0], projectedShadow[v[2]][1], shadowColor, true);

                fillTriangle(canvas, projectedShadow[v[0]][0], projectedShadow[v[0]][1],
                                    projectedShadow[v[2]][0], projectedShadow[v[2]][1],
                                    projectedShadow[v[3]][0], projectedShadow[v[3]][1], shadowColor, true);
            }

            // 6. PAINTER'S DEPTH SORT FOR CUBE FACES
            Integer[] faceOrder = {0, 1, 2, 3, 4, 5};
            double[] faceAverageZ = new double[6];
            for (int f = 0; f < 6; f++) {
                double avgZ = 0.0;
                for (int vIdx : FACES[f]) {
                    avgZ += rotatedZ[vIdx];
                }
                faceAverageZ[f] = avgZ / 4.0;
            }
            java.util.Arrays.sort(faceOrder, (a, b) -> Double.compare(faceAverageZ[b], faceAverageZ[a]));

            // 7. RENDER SOLID CUBE FACES WITH CORRECTED CULLING & LAMBERTIAN SHADING
            for (int f : faceOrder) {
                double nx = NORMALS[f][0];
                double ny = NORMALS[f][1];
                double nz = NORMALS[f][2];

                // Rotate Normals
                double ny1 = ny * Math.cos(angleX) - nz * Math.sin(angleX);
                double nz1 = ny * Math.sin(angleX) + nz * Math.cos(angleX);

                double nx2 = nx * Math.cos(angleY) + nz1 * Math.sin(angleY);
                double nz2 = -nx * Math.sin(angleY) + nz1 * Math.cos(angleY);

                double nx3 = nx2 * Math.cos(angleZ) - ny1 * Math.sin(angleZ);
                double ny3 = nx2 * Math.sin(angleZ) + ny1 * Math.cos(angleZ);

                // FIXED BACK-FACE CULLING DIRECTION: Cull normals pointing away from the camera (nz2 > 0)
                if (nz2 > 0) continue;

                // Shading calculations based on top light
                double dot = nx3 * lx + ny3 * ly + nz2 * lz;
                double ambient = 0.25; // Nice shadow contrast
                double shade = Math.max(0.0, dot);
                double intensity = ambient + (1.0 - ambient) * shade;

                // Shaded color calculation
                int baseColor = FACE_COLORS[f];
                int r = (int) (((baseColor >> 16) & 0xFF) * intensity);
                int g = (int) (((baseColor >> 8) & 0xFF) * intensity);
                int b = (int) ((baseColor & 0xFF) * intensity);
                int shadedColor = (r << 16) | (g << 8) | b;

                // Render solid cube faces (drawn as two triangles each)
                int[] v = FACES[f];
                fillTriangle(canvas, projectedCube[v[0]][0], projectedCube[v[0]][1],
                                    projectedCube[v[1]][0], projectedCube[v[1]][1],
                                    projectedCube[v[2]][0], projectedCube[v[2]][1], shadedColor, false);

                fillTriangle(canvas, projectedCube[v[0]][0], projectedCube[v[0]][1],
                                    projectedCube[v[2]][0], projectedCube[v[2]][1],
                                    projectedCube[v[3]][0], projectedCube[v[3]][1], shadedColor, false);
            }

            // Blit composite buffer to screen
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

    // High-Performance Scanline Triangle Rasterizer blitting solid '█' cells
    private static void fillTriangle(TerminalScene canvas, int x0, int y0, int x1, int y1, int x2, int y2, int color, boolean isShadow) {
        // Sort vertices by Y (y0 <= y1 <= y2)
        if (y0 > y1) { int t = y0; y0 = y1; y1 = t; t = x0; x0 = x1; x1 = t; }
        if (y0 > y2) { int t = y0; y0 = y2; y2 = t; t = x0; x0 = x2; x2 = t; }
        if (y1 > y2) { int t = y1; y1 = y2; y2 = t; t = x1; x1 = x2; x2 = t; }

        int totalHeight = y2 - y0;
        if (totalHeight == 0) return;

        int horizon = canvas.getHeight() / 2;

        for (int y = y0; y <= y2; y++) {
            if (y < 0 || y >= canvas.getHeight()) continue;

            // Shadows are restricted to only paint on the floor (below horizon)
            if (isShadow && y <= horizon) continue;

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
                    // Fetch current cell background to keep depth gradient when drawing transparent-feeling shadows
                    int bgVal = 0x05070A;
                    if (isShadow) {
                        // Dynamically blend shadow with underlying depth floor background!
                        double t = (double) (y - horizon) / (canvas.getHeight() - horizon);
                        int gray = 0x22 + (int) (t * (0xD4 - 0x22));
                        // Darken the floor cells by a factor to cast a beautiful soft shadow!
                        int shadowGray = (int) (gray * 0.45);
                        bgVal = (shadowGray << 16) | (shadowGray << 8) | shadowGray;
                        canvas.writeCell(x, y, ' ', 0x000000, bgVal);
                    } else {
                        // Renders solid shaded block
                        canvas.writeCell(x, y, '█', color, bgVal);
                    }
                }
            }
        }
    }
}
