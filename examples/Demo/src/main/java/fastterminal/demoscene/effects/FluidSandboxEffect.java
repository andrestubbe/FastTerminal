package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;
import java.util.Random;

/**
 * ⏳ Falling Sand Sandbox Physics simulator effect.
 * Performs granular coordinate sorting, gravity cascades, and lateral slides
 * to simulate thousands of falling sand grains building dynamic dunes.
 */
public class FluidSandboxEffect implements DemosceneEffect {

    private int width;
    private int height;

    // Grid states: 0 = Empty, 1 = Sand Type A (Orange), 2 = Sand Type B (Teal)
    private int[][] grid;
    private Random rand = new Random();

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
        grid = new int[height][width];
    }

    @Override
    public void update(long frameIndex) {
        // 1. Spout new sand grains from top spouts
        if (frameIndex % 2 == 0) {
            grid[1][width / 4] = 1;
            grid[1][width / 2] = 2;
            grid[1][3 * width / 4] = 1;
        }

        // 2. Cascade sandbox physics from bottom up
        for (int y = height - 3; y >= 1; y--) {
            for (int x = 1; x < width - 1; x++) {
                int type = grid[y][x];
                if (type > 0) {
                    // Try to fall straight down
                    if (grid[y + 1][x] == 0) {
                        grid[y + 1][x] = type;
                        grid[y][x] = 0;
                    } 
                    // Try to slide diagonally down-left or down-right
                    else {
                        boolean leftFree = grid[y + 1][x - 1] == 0;
                        boolean rightFree = grid[y + 1][x + 1] == 0;

                        if (leftFree && rightFree) {
                            // Choose randomly
                            int side = rand.nextBoolean() ? -1 : 1;
                            grid[y + 1][x + side] = type;
                            grid[y][x] = 0;
                        } else if (leftFree) {
                            grid[y + 1][x - 1] = type;
                            grid[y][x] = 0;
                        } else if (rightFree) {
                            grid[y + 1][x + 1] = type;
                            grid[y][x] = 0;
                        }
                    }
                }
            }
        }

        // Anti-overflow wipe: if pile reaches the top, clear grid!
        boolean overflow = false;
        for (int x = 0; x < width; x++) {
            if (grid[3][x] > 0) overflow = true;
        }
        if (overflow) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    grid[y][x] = 0;
                }
            }
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // Draw obsidian backdrop
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                canvas.writeCell(x, y, ' ', 0, 0x020205);
            }
        }

        // Draw sand dunes
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int type = grid[y][x];
                if (type == 1) {
                    canvas.writeCell(x, y, '▓', 0xEA580C, 0x020205); // Neon Orange Sand
                } else if (type == 2) {
                    canvas.writeCell(x, y, '▓', 0x06B6D4, 0x020205); // Neon Cyan Sand
                }
            }
        }
    }

    @Override
    public String getName() {
        return "⏳ Granular Physics Falling Sand";
    }
}
