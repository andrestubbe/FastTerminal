package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🎨 3D Solid Shaded Studio Cube Effect with dynamic vanishing ground shadow.
 */
public class ShadedEffect implements DemosceneEffect {

    private int width;
    private int height;

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

    private static final int[][] FACES = {
        {4, 5, 6, 7}, // Front face
        {1, 0, 3, 2}, // Back face
        {3, 2, 6, 7}, // Top face
        {0, 1, 5, 4}, // Bottom face
        {1, 5, 6, 2}, // Right face
        {4, 0, 3, 7}  // Left face
    };

    private static final double[][] NORMALS = {
        { 0.0,  0.0,  1.0}, // Front
        { 0.0,  0.0, -1.0}, // Back
        { 0.0,  1.0,  0.0}, // Top
        { 0.0, -1.0,  0.0}, // Bottom
        { 1.0,  0.0,  0.0}, // Right
        {-1.0,  0.0,  0.0}  // Left
    };

    private static final int[] FACE_COLORS = {
        0xF1F5F9, // Front
        0xE2E8F0, // Back
        0xCBD5E1, // Top
        0x94A3B8, // Bottom
        0x64748B, // Right
        0x475569  // Left
    };

    private double angleX = 0.0;
    private double angleY = 0.0;
    private double angleZ = 0.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        angleX += 0.025 / 3.0;
        angleY += 0.035 / 3.0;
        angleZ += 0.015 / 3.0;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        // 1. Draw horizon vanishing floor
        int horizon = (int) (height * 0.70);
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (r < horizon) {
                    canvas.writeCell(c, r, ' ', 0x000000, 0x000000);
                } else {
                    double t = (double) (r - horizon) / (height - horizon);
                    int gray = (int) (t * 0xD4);
                    int floorColor = (gray << 16) | (gray << 8) | gray;
                    canvas.writeCell(c, r, ' ', 0x000000, floorColor);
                }
            }
        }

        // Light setup
        double lx = 0.2; double ly = 1.0; double lz = -0.3;
        double len = Math.sqrt(lx * lx + ly * ly + lz * lz);
        lx /= len; ly /= len; lz /= len;

        double cameraDistance = 3.2;
        double yTranslation = -0.2;
        double floorY = 2.0;

        int[][] projectedCube = new int[VERTICES.length][2];
        int[][] projectedShadow = new int[VERTICES.length][2];
        double[] rotatedZ = new double[VERTICES.length];

        // 2. Rotate and project
        for (int i = 0; i < VERTICES.length; i++) {
            double x = VERTICES[i][0];
            double y = VERTICES[i][1];
            double z = VERTICES[i][2];

            double y1 = y * Math.cos(angleX) - z * Math.sin(angleX);
            double z1 = y * Math.sin(angleX) + z * Math.cos(angleX);

            double x2 = x * Math.cos(angleY) + z1 * Math.sin(angleY);
            double z2 = -x * Math.sin(angleY) + z1 * Math.cos(angleY);

            double x3 = x2 * Math.cos(angleZ) - y1 * Math.sin(angleZ);
            double y3 = x2 * Math.sin(angleZ) + y1 * Math.cos(angleZ);

            double cubeY = y3 + yTranslation;
            rotatedZ[i] = z2;

            double scaleCube = (height * 0.5) / (cameraDistance + z2);
            projectedCube[i][0] = (int) (width / 2.0 + x3 * scaleCube * 2.1);
            projectedCube[i][1] = (int) (height / 2.0 + cubeY * scaleCube);

            double scaleShadow = (height * 0.5) / (cameraDistance + z2);
            projectedShadow[i][0] = (int) (width / 2.0 + x3 * scaleShadow * 2.1);
            projectedShadow[i][1] = (int) (height / 2.0 + floorY * scaleShadow);
        }

        // 3. Draw cast floor shadow
        int shadowColor = 0x1E293B;
        for (int f = 0; f < 6; f++) {
            int[] v = FACES[f];
            fillTriangle(canvas, projectedShadow[v[0]][0], projectedShadow[v[0]][1],
                                projectedShadow[v[1]][0], projectedShadow[v[1]][1],
                                projectedShadow[v[2]][0], projectedShadow[v[2]][1], shadowColor, true, horizon);

            fillTriangle(canvas, projectedShadow[v[0]][0], projectedShadow[v[0]][1],
                                projectedShadow[v[2]][0], projectedShadow[v[2]][1],
                                projectedShadow[v[3]][0], projectedShadow[v[3]][1], shadowColor, true, horizon);
        }

        // 4. Painter's depth sort
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

        // 5. Draw solid shaded cube faces
        for (int f : faceOrder) {
            double nx = NORMALS[f][0];
            double ny = NORMALS[f][1];
            double nz = NORMALS[f][2];

            double ny1 = ny * Math.cos(angleX) - nz * Math.sin(angleX);
            double nz1 = ny * Math.sin(angleX) + nz * Math.cos(angleX);

            double nx2 = nx * Math.cos(angleY) + nz1 * Math.sin(angleY);
            double nz2 = -nx * Math.sin(angleY) + nz1 * Math.cos(angleY);

            double nx3 = nx2 * Math.cos(angleZ) - ny1 * Math.sin(angleZ);
            double ny3 = nx2 * Math.sin(angleZ) + ny1 * Math.cos(angleZ);

            if (nz2 > 0) continue; // Back-face culling

            double dot = nx3 * lx + ny3 * ly + nz2 * lz;
            double ambient = 0.25;
            double shade = Math.max(0.0, dot);
            double intensity = ambient + (1.0 - ambient) * shade;

            int baseColor = FACE_COLORS[f];
            int r = (int) (((baseColor >> 16) & 0xFF) * intensity);
            int g = (int) (((baseColor >> 8) & 0xFF) * intensity);
            int b = (int) ((baseColor & 0xFF) * intensity);
            int color = (r << 16) | (g << 8) | b;

            int[] v = FACES[f];
            fillTriangle(canvas, projectedCube[v[0]][0], projectedCube[v[0]][1],
                                projectedCube[v[1]][0], projectedCube[v[1]][1],
                                projectedCube[v[2]][0], projectedCube[v[2]][1], color, false, horizon);

            fillTriangle(canvas, projectedCube[v[0]][0], projectedCube[v[0]][1],
                                projectedCube[v[2]][0], projectedCube[v[2]][1],
                                projectedCube[v[3]][0], projectedCube[v[3]][1], color, false, horizon);
        }
    }

    private void fillTriangle(FastTerminalScene canvas, int x0, int y0, int x1, int y1, int x2, int y2, int color, boolean isShadow, int horizon) {
        if (y0 > y1) { int t = y0; y0 = y1; y1 = t; t = x0; x0 = x1; x1 = t; }
        if (y0 > y2) { int t = y0; y0 = y2; y2 = t; t = x0; x0 = x2; x2 = t; }
        if (y1 > y2) { int t = y1; y1 = y2; y2 = t; t = x1; x1 = x2; x2 = t; }

        int totalHeight = y2 - y0;
        if (totalHeight == 0) return;

        for (int y = y0; y <= y2; y++) {
            if (y < 0 || y >= height) continue;
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
                if (x >= 0 && x < width) {
                    if (isShadow) {
                        double t = (double) (y - horizon) / (height - horizon);
                        int gray = (int) (t * 0xD4);
                        int shadowGray = (int) (gray * 0.45);
                        int bgVal = (shadowGray << 16) | (shadowGray << 8) | shadowGray;
                        canvas.writeCell(x, y, ' ', 0x000000, bgVal);
                    } else {
                        canvas.writeCell(x, y, '█', color, 0x05070A);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🎨 3D Solid Shaded Studio Cube";
    }
}
