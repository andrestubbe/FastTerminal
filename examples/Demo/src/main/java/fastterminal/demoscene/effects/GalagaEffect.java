package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;
import java.util.Random;

/**
 * 🚀 Galaga / Space Invaders retro simulation mini-game.
 * Renders hero cruiser, alien squadron grid, high-velocity laser beams,
 * moving starfields, and glowing particle shockwaves during hits.
 */
public class GalagaEffect implements DemosceneEffect {

    private int width;
    private int height;

    // Simulation states
    private double heroX;
    private double heroTargetX;
    private double alienX;
    private double alienDir = 1.0;
    
    // Lasers: laserY <= 0 means inactive
    private static final int MAX_LASERS = 12;
    private double[] laserX = new double[MAX_LASERS];
    private double[] laserY = new double[MAX_LASERS];
    private boolean[] isAlienLaser = new boolean[MAX_LASERS];

    // Explosion particles
    private static final int MAX_PARTICLES = 40;
    private double[] px = new double[MAX_PARTICLES];
    private double[] py = new double[MAX_PARTICLES];
    private double[] vx = new double[MAX_PARTICLES];
    private double[] vy = new double[MAX_PARTICLES];
    private int[] age = new int[MAX_PARTICLES];
    private int[] maxAge = new int[MAX_PARTICLES];

    // Aliens grid: active or dead
    private static final int ALIEN_ROWS = 3;
    private static final int ALIEN_COLS = 6;
    private boolean[][] alienActive = new boolean[ALIEN_ROWS][ALIEN_COLS];

    private Random rand = new Random();

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        heroX = width / 2.0;
        heroTargetX = heroX;
        alienX = width * 0.2;

        // Reset lasers
        for (int i = 0; i < MAX_LASERS; i++) {
            laserY[i] = -1.0;
        }

        // Reset particles
        for (int i = 0; i < MAX_PARTICLES; i++) {
            age[i] = 999;
        }

        // Respawn alien squad grid
        respawnAliens();
    }

    private void respawnAliens() {
        for (int r = 0; r < ALIEN_ROWS; r++) {
            for (int c = 0; c < ALIEN_COLS; c++) {
                alienActive[r][c] = true;
            }
        }
    }

    private void spawnExplosion(double x, double y) {
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (age[i] >= maxAge[i]) {
                px[i] = x;
                py[i] = y;
                double angle = rand.nextDouble() * 2.0 * Math.PI;
                double speed = 0.2 + rand.nextDouble() * 0.5;
                vx[i] = Math.cos(angle) * speed;
                vy[i] = Math.sin(angle) * speed * 0.5; // aspect scale
                age[i] = 0;
                maxAge[i] = 15 + rand.nextInt(15);
            }
        }
    }

    @Override
    public void update(long frameIndex) {
        // 1. Move hero AI towards targets
        if (frameIndex % 30 == 0) {
            heroTargetX = width * 0.25 + rand.nextDouble() * width * 0.5;
        }
        heroX += (heroTargetX - heroX) * 0.08;

        // 2. Move alien grid back and forth
        alienX += alienDir * 0.15;
        if (alienX < width * 0.1 || alienX > width * 0.6) {
            alienDir = -alienDir;
        }

        // 3. Automated firing rates
        if (frameIndex % 8 == 0) {
            // Hero shoots
            for (int i = 0; i < MAX_LASERS; i++) {
                if (laserY[i] < 0) {
                    laserX[i] = heroX;
                    laserY[i] = height - 4;
                    isAlienLaser[i] = false;
                    break;
                }
            }
        }

        if (frameIndex % 15 == 0) {
            // Random alien shoots
            int r = rand.nextInt(ALIEN_ROWS);
            int c = rand.nextInt(ALIEN_COLS);
            if (alienActive[r][c]) {
                for (int i = 0; i < MAX_LASERS; i++) {
                    if (laserY[i] < 0) {
                        laserX[i] = alienX + c * 6.0;
                        laserY[i] = 3.0 + r * 2.0;
                        isAlienLaser[i] = true;
                        break;
                    }
                }
            }
        }

        // 4. Update lasers positions and check hits
        boolean anyAlienAlive = false;
        for (int r = 0; r < ALIEN_ROWS; r++) {
            for (int c = 0; c < ALIEN_COLS; c++) {
                if (alienActive[r][c]) anyAlienAlive = true;
            }
        }
        if (!anyAlienAlive) respawnAliens();

        for (int i = 0; i < MAX_LASERS; i++) {
            if (laserY[i] >= 0) {
                if (isAlienLaser[i]) {
                    laserY[i] += 0.8; // move down
                    if (laserY[i] >= height) laserY[i] = -1.0;
                } else {
                    laserY[i] -= 0.9; // move up
                    if (laserY[i] < 0) {
                        laserY[i] = -1.0;
                    } else {
                        // Check collision with aliens
                        for (int r = 0; r < ALIEN_ROWS; r++) {
                            for (int c = 0; c < ALIEN_COLS; c++) {
                                if (alienActive[r][c]) {
                                    double ax = alienX + c * 6.0;
                                    double ay = 3.0 + r * 2.0;
                                    if (Math.abs(laserX[i] - ax) < 3.0 && Math.abs(laserY[i] - ay) < 1.2) {
                                        alienActive[r][c] = false;
                                        spawnExplosion(ax, ay);
                                        laserY[i] = -1.0;
                                        break;
                                    }
                                }
                            }
                            if (laserY[i] < 0) break;
                        }
                    }
                }
            }
        }

        // 5. Update explosion particles
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (age[i] < maxAge[i]) {
                age[i]++;
                px[i] += vx[i];
                py[i] += vy[i];
            }
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // Background space void
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Dim drifting star dust
                if ((x * 7 + y * 13 + (int)(System.currentTimeMillis() / 250)) % 67 == 0) {
                    canvas.writeCell(x, y, '·', 0x475569, 0x010103);
                } else {
                    canvas.writeCell(x, y, ' ', 0, 0x010103);
                }
            }
        }

        // 1. Draw Hero Ship (Cyan)
        int hx = (int) Math.round(heroX);
        int hy = height - 3;
        canvas.writeString(hx - 2, hy, " █▄█ ", 0x06B6D4, 0x010103);
        canvas.writeString(hx - 2, hy + 1, "▀███▀", 0x0891B2, 0x010103);

        // 2. Draw Alien squadron (Red-Pink)
        for (int r = 0; r < ALIEN_ROWS; r++) {
            for (int c = 0; c < ALIEN_COLS; c++) {
                if (alienActive[r][c]) {
                    int ax = (int) Math.round(alienX + c * 6.0);
                    int ay = 3 + r * 2;
                    canvas.writeString(ax - 2, ay, "▄███▄", 0xEF4444, 0x010103);
                    canvas.writeString(ax - 2, ay + 1, " ▀ ▀ ", 0xEC4899, 0x010103);
                }
            }
        }

        // 3. Draw Lasers (Hero = Cyan '|', Alien = Red '▼')
        for (int i = 0; i < MAX_LASERS; i++) {
            if (laserY[i] >= 0) {
                int lx = (int) Math.round(laserX[i]);
                int ly = (int) Math.round(laserY[i]);
                if (lx >= 0 && lx < width && ly >= 0 && ly < height) {
                    if (isAlienLaser[i]) {
                        canvas.writeCell(lx, ly, '▼', 0xEF4444, 0x010103);
                    } else {
                        canvas.writeCell(lx, ly, '║', 0x06B6D4, 0x010103);
                    }
                }
            }
        }

        // 4. Draw explosion particles (Gold-Orange sparks)
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (age[i] < maxAge[i]) {
                int sx = (int) Math.round(px[i]);
                int sy = (int) Math.round(py[i]);
                if (sx >= 0 && sx < width && sy >= 0 && sy < height) {
                    double t = (double) age[i] / maxAge[i];
                    int color = (t < 0.4) ? 0xFDE047 : (t < 0.75) ? 0xEA580C : 0x7F1D1D;
                    char glyph = (t < 0.4) ? '*' : '·';
                    canvas.writeCell(sx, sy, glyph, color, 0x010103);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "🚀 Galaga Space Invaders Combat";
    }
}
