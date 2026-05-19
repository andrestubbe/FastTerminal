package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🧱 Real-Time 3D Raycaster (Wolfenstein 3D style).
 * Projects rays across a 60° FOV through a 16x16 maze, computes intersection distance,
 * draws perspective walls with depth-shading, and overlaps a real-time radar mini-map.
 */
public class RaycasterEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double playerX = 2.5;
    private double playerY = 2.5;
    private double playerA = 0.0; // Angle

    // 16x16 Map: '#' = Wall, '.' = Space
    private static final String[] MAP = {
        "################",
        "#.#............#",
        "#.#.########.#.#",
        "#.#........#.#.#",
        "#.########.#.#.#",
        "#.#........#.#.#",
        "#.#.########.#.#",
        "#.#..........#.#",
        "#.############.#",
        "#............#.#",
        "#.##########.#.#",
        "#.#........#.#.#",
        "#.#.########.#.#",
        "#.#..........#.#",
        "#.############.#",
        "################"
    };

    private static final int MAP_SIZE = 16;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        // Procedural pathfinding camera moving through the maze
        double t = frameIndex * 0.008;
        playerX = 8.0 + 5.0 * Math.sin(t * 1.3);
        playerY = 8.0 + 5.0 * Math.cos(t * 0.9);
        playerA = t * 2.0;

        // Keep player safely inside boundaries
        if (playerX < 1.5) playerX = 1.5;
        if (playerX > 14.5) playerX = 14.5;
        if (playerY < 1.5) playerY = 1.5;
        if (playerY > 14.5) playerY = 14.5;
        
        int mapX = (int) playerX;
        int mapY = (int) playerY;
        if (MAP[mapY].charAt(mapX) == '#') {
            // Push player out of walls
            playerX = 8.0;
            playerY = 8.0;
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // 1. Draw sky & floor background
        int halfHeight = height / 2;
        for (int y = 0; y < height; y++) {
            int bgColor = (y < halfHeight) ? 0x050510 : 0x111827; // Space blue sky, dark slate floor
            for (int x = 0; x < width; x++) {
                canvas.writeCell(x, y, ' ', 0, bgColor);
            }
        }

        // 2. Cast rays across FOV (60 degrees)
        double fov = Math.PI / 3.0;
        double startAngle = playerA - fov / 2.0;

        for (int x = 0; x < width; x++) {
            double rayAngle = startAngle + (x / (double) width) * fov;

            double distanceToWall = 0.0;
            boolean hitWall = false;

            double eyeX = Math.cos(rayAngle);
            double eyeY = Math.sin(rayAngle);

            while (!hitWall && distanceToWall < 16.0) {
                distanceToWall += 0.08;

                int testX = (int) (playerX + eyeX * distanceToWall);
                int testY = (int) (playerY + eyeY * distanceToWall);

                // Boundary checks
                if (testX < 0 || testX >= MAP_SIZE || testY < 0 || testY >= MAP_SIZE) {
                    hitWall = true;
                    distanceToWall = 16.0;
                } else if (MAP[testY].charAt(testX) == '#') {
                    hitWall = true;
                }
            }

            // Fisheye correction
            double correctedDist = distanceToWall * Math.cos(rayAngle - playerA);

            // Compute perspective wall heights
            int wallHeight = (int) (height / (correctedDist + 0.01));
            int ceiling = halfHeight - wallHeight / 2;
            int floor = halfHeight + wallHeight / 2;

            // Depth shadow shading (closer = bright copper gold, further = dark charcoal)
            double shade = 1.0 - (distanceToWall / 16.0);
            if (shade < 0) shade = 0;

            int r = (int) (245 * shade);
            int g = (int) (158 * shade);
            int b = (int) (11 * shade);
            int color = (r << 16) | (g << 8) | b;

            char wallGlyph = (distanceToWall < 4.0) ? '█' : (distanceToWall < 7.0) ? '▓' : (distanceToWall < 10.0) ? '▒' : '░';

            for (int y = 0; y < height; y++) {
                if (y >= ceiling && y < floor) {
                    canvas.writeCell(x, y, wallGlyph, color, 0x050510);
                }
            }
        }

        // 3. Render 2D radar mini-map overlay (in top-right corner)
        int mapScaleX = 2;
        int mapScaleY = 1;
        int offsetX = width - MAP_SIZE * mapScaleX - 2;
        int offsetY = 2;

        if (offsetX > 0 && width >= 40 && height >= 20) {
            // Draw map grid
            for (int my = 0; my < MAP_SIZE; my++) {
                for (int mx = 0; mx < MAP_SIZE; mx++) {
                    int sx = offsetX + mx * mapScaleX;
                    int sy = offsetY + my * mapScaleY;

                    char tile = MAP[my].charAt(mx);
                    int fg = (tile == '#') ? 0x475569 : 0x1E293B;
                    int bg = (tile == '#') ? 0x334155 : 0x0F172A;

                    canvas.writeString(sx, sy, "  ", fg, bg);
                }
            }

            // Draw player dot on radar
            int pxOnMap = offsetX + (int) playerX * mapScaleX;
            int pyOnMap = offsetY + (int) playerY * mapScaleY;
            if (pxOnMap >= offsetX && pxOnMap < offsetX + MAP_SIZE * mapScaleX &&
                pyOnMap >= offsetY && pyOnMap < offsetY + MAP_SIZE * mapScaleY) {
                canvas.writeString(pxOnMap, pyOnMap, "▲▲", 0xEF4444, 0x0F172A); // Bright red direction arrow
            }
        }
    }

    @Override
    public String getName() {
        return "🧱 Real-Time 3D Raycaster Grid Maze";
    }
}
