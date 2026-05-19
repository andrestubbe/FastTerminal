package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🌄 Voxel 3D Terrain Landscape flight effect.
 * Projects mathematical elevation fields onto a 3D perspective grid,
 * flying endlessly over retro mountains with a giant glowing neon horizon sun.
 */
public class TerrainEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double flightPhase = 0.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        flightPhase = frameIndex * 0.08;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // 1. Draw glowing space nebula sky and Synthwave Sunset Sun
        int horizonY = (int) (height * 0.45);
        double sunRadius = height * 0.22;
        double aspect = 2.0;

        for (int y = 0; y < horizonY; y++) {
            double dy = y - horizonY * 0.6;
            for (int x = 0; x < width; x++) {
                double dx = (x - width / 2.0) / aspect;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < sunRadius) {
                    // Golden Sun with horizontal scanline gaps (retrowave style!)
                    if ((y % 3) != 0) {
                        canvas.writeCell(x, y, '█', 0xF59E0B, 0x05020F); // Orange sun
                    } else {
                        canvas.writeCell(x, y, ' ', 0, 0x05020F);
                    }
                } else {
                    // Deep cosmic violet backdrop
                    canvas.writeCell(x, y, ' ', 0, 0x05020F);
                }
            }
        }

        // 2. Draw 3D Voxel Mountains Grid
        int gridX = 26;
        int gridZ = 18;

        for (int gz = gridZ - 1; gz >= 2; gz--) {
            // Forward flight movement offset
            double z3 = gz - (flightPhase % 1.0);
            double depthShading = 1.0 - (z3 / gridZ);
            if (depthShading < 0) depthShading = 0;

            // Perspective scale
            double scale = (height * 0.55) / z3;

            for (int gx = 0; gx < gridX; gx++) {
                double x3 = (gx - gridX / 2.0) * 1.4;

                // Terrain elevation height
                double terrainHeight = 0.4 * Math.sin(gx * 0.5) * Math.cos(z3 * 0.4 + flightPhase * 0.2)
                                     + 0.2 * Math.sin(gx * 1.2 + flightPhase * 0.1);

                // Perspective screen project
                int screenX = (int) (width / 2.0 + x3 * scale * aspect);
                int screenY = (int) (height * 0.65 - terrainHeight * scale);

                if (screenX >= 0 && screenX < width && screenY >= horizonY && screenY < height) {
                    // Color mapping based on height and depth (peaks are white/cyan, valleys are purple)
                    int r, g, b;
                    if (terrainHeight > 0.1) { // Mountain Peaks (Bright Cyan)
                        r = (int) (6 * depthShading);
                        g = (int) (182 * depthShading);
                        b = (int) (212 * depthShading);
                    } else { // Valleys (Deep Violet)
                        r = (int) (139 * depthShading);
                        g = (int) (92 * depthShading);
                        b = (int) (246 * depthShading);
                    }

                    int color = (r << 16) | (g << 8) | b;
                    char glyph = (terrainHeight > 0.2) ? '▲' : (terrainHeight < -0.1) ? '▼' : '■';

                    // Fill screen column downward from projected peak to ground floor
                    for (int y = screenY; y < height; y++) {
                        canvas.writeCell(screenX, y, glyph, color, 0x05020F);
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🌄 Voxel 3D Synthwave Landscape";
    }
}
