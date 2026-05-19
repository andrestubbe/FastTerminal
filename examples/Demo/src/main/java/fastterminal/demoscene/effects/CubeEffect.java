package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;
import java.util.Random;

/**
 * @class CubeEffect
 * @brief 🧊 3D Wireframe Tumbling Cube Effect with dynamic starfield background.
 * 
 * Simulates a volumetric curved depth tunnel with spiral starfields, while rendering
 * a fully rotatable 3D wireframe cube using perspective projection and aspect-ratio compensation.
 * Utilizes subpixel coordinate lines mapped to double vertical half-blocks (▄).
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

    // Curved warp tunnel background parameters
    private static final int CUBE_STAR_COUNT = 200;
    private double[] starX = new double[CUBE_STAR_COUNT];
    private double[] starY = new double[CUBE_STAR_COUNT];
    private double[] starZ = new double[CUBE_STAR_COUNT];
    private double phase = 0.0;

    private double angleX = 0.0;
    private double angleY = 0.0;
    private double angleZ = 0.0;
    private double colorPhase = 0.0;

    /**
     * @brief Initializes view sizes and spawns background 3D star coordinates.
     * 
     * @param width Terminal screen width.
     * @param height Terminal screen height.
     */
    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        Random rand = new Random();
        // Populate 3D space coordinates for background curved warp tunnel stars
        for (int i = 0; i < CUBE_STAR_COUNT; i++) {
            starX[i] = (rand.nextDouble() - 0.5) * 40.0;
            starY[i] = (rand.nextDouble() - 0.5) * 20.0;
            starZ[i] = 0.1 + rand.nextDouble() * 20.0; // Distance
        }
    }

    /**
     * @brief Computes 3D cube rotation angles and scrolls background stars.
     * 
     * Star Z coordinates are decremented to simulate high-speed forward movement.
     * 
     * @param frameIndex Monotonically increasing frame index.
     */
    @Override
    public void update(long frameIndex) {
        // Tumbling rotations matched to original demo
        angleX += 0.025 / 3.0;
        angleY += 0.035 / 3.0;
        angleZ += 0.015 / 3.0;
        colorPhase += 0.04 / 3.0;

        // Curved warp tunnel background updates (slowed to exactly 1/2 of 0.15 speed = 0.075!)
        phase = frameIndex * 0.02;
        Random rand = new Random();
        for (int i = 0; i < CUBE_STAR_COUNT; i++) {
            starZ[i] -= 0.0375; // Slowed to exactly 1/2 of 0.075 speed!

            if (starZ[i] <= 0.1) {
                starX[i] = (rand.nextDouble() - 0.5) * 40.0;
                starY[i] = (rand.nextDouble() - 0.5) * 20.0;
                starZ[i] = 20.0;
            }
        }
    }

    /**
     * @brief Transforms 3D coordinates, rasters edges, and flushes subpixel buffer to screen.
     * 
     * @param canvas Double-buffer render target.
     */
    @Override
    public void render(FastTerminalScene canvas) {
        int doubleHeight = 2 * height;
        int[] subpixelColors = new int[width * doubleHeight];
        java.util.Arrays.fill(subpixelColors, 0x010105); // Deep space background

        double centerX = width / 2.0;
        double centerY = height; // doubled vertical center
        double aspect = 2.0;

        // 1. Draw curving tunnel background stars with volumetric depth fog
        double tunnelCenterX = centerX + 10.0 * Math.sin(phase * 1.5);
        double tunnelCenterY = centerY + 10.0 * Math.cos(phase * 0.8);

        for (int i = 0; i < CUBE_STAR_COUNT; i++) {
            double z = starZ[i];
            double scale = (height * 1.2) / z;

            // Apply spiral angle rotation based on depth Z
            double angleOffset = phase + z * 0.12;
            double cos = Math.cos(angleOffset);
            double sin = Math.sin(angleOffset);

            double rx = starX[i] * cos - starY[i] * sin;
            double ry = starX[i] * sin + starY[i] * cos;

            int px = (int) (tunnelCenterX + rx * scale * aspect);
            int py = (int) (tunnelCenterY + ry * scale);

            if (px >= 0 && px < width && py >= 0 && py < doubleHeight) {
                double depth = 1.0 - (z / 20.0);
                if (depth < 0) depth = 0;

                int color;
                if (depth > 0.8) {
                    color = 0xFFFFFF; // White spark
                } else if (depth > 0.5) {
                    color = 0x22D3EE; // Cyber Cyan
                } else {
                    color = 0x8B5CF6; // Violet
                }

                // Apply volumetric 3D perspective fog-to-black blend
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;
                r = (int) (r * depth);
                g = (int) (g * depth);
                b = (int) (b * depth);
                color = (r << 16) | (g << 8) | b;

                subpixelColors[px + py * width] = color;
            }
        }

        // 2. Transform vertices with aspect correction (1.05 horizontal multiplier to compensate for double height)
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

            double scale = (height * 1.2) / (cameraDistance + z2); // Scaled for double height
            projected[i][0] = (int) (width / 2.0 + x3 * scale * 1.05); // Correct aspect ratio!
            projected[i][1] = (int) (doubleHeight / 2.0 + y3 * scale);
        }

        // 3. Draw edge lines using Bresenham algorithm on double-res buffer
        for (int i = 0; i < EDGES.length; i++) {
            int p0 = EDGES[i][0];
            int p1 = EDGES[i][1];

            int x0 = projected[p0][0];
            int y0 = projected[p0][1];
            int x1 = projected[p1][0];
            int y1 = projected[p1][1];

            int edgeColor = getNeonColor(colorPhase + (double) i * 0.5);
            drawLineToBuffer(subpixelColors, width, doubleHeight, x0, y0, x1, y1, edgeColor);
        }

        // 4. Highlight vertices with bright white overlays
        for (int i = 0; i < VERTICES.length; i++) {
            int vx = projected[i][0];
            int vy = projected[i][1];
            if (vx >= 0 && vx < width && vy >= 0 && vy < doubleHeight) {
                subpixelColors[vx + vy * width] = 0xFFFFFF; // Bright white vertex dot
            }
        }

        // 5. Draw double-resolution buffer using half-blocks
        for (int row = 0; row < height; row++) {
            int yTop = 2 * row;
            int yBot = 2 * row + 1;

            for (int x = 0; x < width; x++) {
                int colorTop = subpixelColors[x + yTop * width];
                int colorBot = subpixelColors[x + yBot * width];

                canvas.writeCell(x, row, '▄', colorBot, colorTop);
            }
        }
    }

    /**
     * @brief Computes dynamic neon color phases from a radial hue shift.
     * 
     * @param huePhase Wave parameter representing hue offset.
     * @return 24-bit packed RGB color.
     */
    private int getNeonColor(double huePhase) {
        int r = (int) (Math.sin(huePhase) * 110 + 145);
        int g = (int) (Math.sin(huePhase + 2.0 * Math.PI / 3.0) * 110 + 145);
        int b = (int) (Math.sin(huePhase + 4.0 * Math.PI / 3.0) * 110 + 145);
        return (r << 16) | (g << 8) | b;
    }

    private void blendPixel(int[] buffer, int w, int h, int x, int y, double alpha, int color) {
        if (x >= 0 && x < w && y >= 0 && y < h) {
            int idx = x + y * w;
            int bg = buffer[idx];
            
            int rBg = (bg >> 16) & 0xFF;
            int gBg = (bg >> 8) & 0xFF;
            int bBg = bg & 0xFF;
            
            int rFg = (color >> 16) & 0xFF;
            int gFg = (color >> 8) & 0xFF;
            int bFg = color & 0xFF;
            
            int r = (int) (rFg * alpha + rBg * (1.0 - alpha));
            int g = (int) (gFg * alpha + gBg * (1.0 - alpha));
            int b = (int) (bFg * alpha + bBg * (1.0 - alpha));
            
            buffer[idx] = (r << 16) | (g << 8) | b;
        }
    }

    /**
     * @brief Anti-aliased 2D line drawer implementing Xiaolin Wu's algorithm.
     * 
     * @param buffer Subpixel integer array canvas.
     * @param w Horizontal canvas buffer width.
     * @param h Vertical canvas buffer height.
     * @param x0 Starting horizontal coordinate.
     * @param y0 Starting vertical coordinate.
     * @param x1 Ending horizontal coordinate.
     * @param y1 Ending vertical coordinate.
     * @param color 24-bit packed RGB color to write.
     */
    private void drawLineToBuffer(int[] buffer, int w, int h, int x0, int y0, int x1, int y1, int color) {
        boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
        if (steep) {
            int temp = x0; x0 = y0; y0 = temp;
            temp = x1; x1 = y1; y1 = temp;
        }
        if (x0 > x1) {
            int temp = x0; x0 = x1; x1 = temp;
            temp = y0; y0 = y1; y1 = temp;
        }

        double dx = x1 - x0;
        double dy = y1 - y0;
        double gradient = (dx == 0.0) ? 1.0 : dy / dx;

        // Handle first endpoint
        double xend = Math.round(x0);
        double yend = y0 + gradient * (xend - x0);
        double xgap = 1.0 - ((x0 + 0.5) - Math.floor(x0 + 0.5));
        int xpx1 = (int) xend;
        int ypx1 = (int) Math.floor(yend);
        
        if (steep) {
            blendPixel(buffer, w, h, ypx1, xpx1, (1.0 - (yend - ypx1)) * xgap, color);
            blendPixel(buffer, w, h, ypx1 + 1, xpx1, (yend - ypx1) * xgap, color);
        } else {
            blendPixel(buffer, w, h, xpx1, ypx1, (1.0 - (yend - ypx1)) * xgap, color);
            blendPixel(buffer, w, h, xpx1, ypx1 + 1, (yend - ypx1) * xgap, color);
        }
        double intery = yend + gradient;

        // Handle second endpoint
        xend = Math.round(x1);
        yend = y1 + gradient * (xend - x1);
        xgap = (x1 + 0.5) - Math.floor(x1 + 0.5);
        int xpx2 = (int) xend;
        int ypx2 = (int) Math.floor(yend);
        
        if (steep) {
            blendPixel(buffer, w, h, ypx2, xpx2, (1.0 - (yend - ypx2)) * xgap, color);
            blendPixel(buffer, w, h, ypx2 + 1, xpx2, (yend - ypx2) * xgap, color);
        } else {
            blendPixel(buffer, w, h, xpx2, ypx2, (1.0 - (yend - ypx2)) * xgap, color);
            blendPixel(buffer, w, h, xpx2, ypx2 + 1, (yend - ypx2) * xgap, color);
        }

        // Main loop
        if (steep) {
            for (int x = xpx1 + 1; x < xpx2; x++) {
                int y = (int) Math.floor(intery);
                blendPixel(buffer, w, h, y, x, 1.0 - (intery - y), color);
                blendPixel(buffer, w, h, y + 1, x, intery - y, color);
                intery += gradient;
            }
        } else {
            for (int x = xpx1 + 1; x < xpx2; x++) {
                int y = (int) Math.floor(intery);
                blendPixel(buffer, w, h, x, y, 1.0 - (intery - y), color);
                blendPixel(buffer, w, h, x, y + 1, intery - y, color);
                intery += gradient;
            }
        }
    }

    /**
     * @brief Returns the visual user-friendly name of the effect.
     * @return String effect name label.
     */
    @Override
    public String getName() {
        return "3D Wireframe Tumbling Cube";
    }
}
