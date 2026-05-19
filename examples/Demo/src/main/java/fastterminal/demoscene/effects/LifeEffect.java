package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;
import java.util.Random;

/**
 * 🧬 Self-Sustaining Cyber Conway's Game of Life.
 * Simulates standard cellular automata rules at 120 FPS, shading cells by age,
 * and automatically seeding space gliders to keep the ecosystem active forever.
 */
public class LifeEffect implements DemosceneEffect {

    private int width;
    private int height;

    private boolean[][] grid;
    private boolean[][] nextGrid;
    private int[][] cellAge;

    private Random rand = new Random();

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        grid = new boolean[height][width];
        nextGrid = new boolean[height][width];
        cellAge = new int[height][width];

        // Seed initial organic distribution block
        seedRandomBlocks();
    }

    private void seedRandomBlocks() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = rand.nextDouble() < 0.22;
                cellAge[y][x] = grid[y][x] ? 1 : 0;
            }
        }
    }

    private void spawnGlider(int targetX, int targetY) {
        // Standard Gosper glider shape offsets
        int[][] gliderOffsets = {
            {0, 1}, {1, 2}, {2, 0}, {2, 1}, {2, 2}
        };

        for (int[] offset : gliderOffsets) {
            int x = (targetX + offset[0] + width) % width;
            int y = (targetY + offset[1] + height) % height;
            grid[y][x] = true;
            cellAge[y][x] = 1;
        }
    }

    @Override
    public void update(long frameIndex) {
        // 1. Spawning injection gates to keep ecosystem alive forever!
        if (frameIndex % 80 == 0) {
            // Spawn random gliders
            spawnGlider(rand.nextInt(width), rand.nextInt(height));
        }

        int activeCount = 0;

        // 2. Evaluate Conway's rules: B3/S23
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int neighbors = countNeighbors(x, y);
                boolean isAlive = grid[y][x];

                if (isAlive) {
                    if (neighbors < 2 || neighbors > 3) {
                        nextGrid[y][x] = false; // Underpopulation / Overpopulation
                    } else {
                        nextGrid[y][x] = true; // Survival
                        cellAge[y][x]++;
                    }
                } else {
                    if (neighbors == 3) {
                        nextGrid[y][x] = true; // Reproduction
                        cellAge[y][x] = 1;
                    } else {
                        nextGrid[y][x] = false;
                        cellAge[y][x] = 0;
                    }
                }

                if (nextGrid[y][x]) activeCount++;
            }
        }

        // Swap buffers
        boolean[][] temp = grid;
        grid = nextGrid;
        nextGrid = temp;

        // Anti-extinction check: if too few cells, re-seed!
        if (activeCount < 15) {
            seedRandomBlocks();
        }
    }

    private int countNeighbors(int cx, int cy) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            int ny = (cy + dy + height) % height;
            for (int dx = -1; dx <= 1; dx++) {
                if (dy == 0 && dx == 0) continue;
                int nx = (cx + dx + width) % width;
                if (grid[ny][nx]) count++;
            }
        }
        return count;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (grid[y][x]) {
                    int ageVal = cellAge[y][x];

                    // Aging color mapping: Newborn = Gold, Survived = Hot Pink, Ancient = Bright Cyan
                    int color;
                    if (ageVal < 3) {
                        color = 0xF59E0B; // Newborn gold
                    } else if (ageVal < 10) {
                        color = 0xEC4899; // Surviving pink
                    } else {
                        color = 0x06B6D4; // Ancient cyan
                    }

                    char glyph = (ageVal < 3) ? '░' : (ageVal < 10) ? '▓' : '█';
                    canvas.writeCell(x, y, glyph, color, 0x020105);
                } else {
                    canvas.writeCell(x, y, ' ', 0, 0x010002);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🧬 Self-Sustaining Cyber Conway's Life";
    }
}
