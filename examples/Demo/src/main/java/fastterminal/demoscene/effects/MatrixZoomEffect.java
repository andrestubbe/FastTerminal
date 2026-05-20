package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;
import java.util.Random;

/**
 * @class MatrixZoomEffect
 * @brief 💊 Infinite cinematic fractal zoom into Matrix symbols.
 * 
 * Simulates classic Matrix falling digital rain. At regular intervals, the rain morphs 
 * to reveal a beautiful green cybernetic symbol in the center. The camera then 
 * pans and zooms exponentially into a single pixel of the symbol, flying deep inside 
 * until the screen is once again completely filled with standard falling letters.
 */
public class MatrixZoomEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double time = 0.0;
    private Random random = new Random();

    // Standard falling rain streams
    private double[] ambientHeads;
    private double[] ambientSpeeds;

    // Beautiful High-Contrast 16x16 Bitmaps
    private static final String[][] SYMBOLS = {
        { // 1. Traditional Matrix Katakana 'ｦ' representation
            "    ████████    ",
            "   ██      ██   ",
            "  ██   ██   ██  ",
            " ██████████████ ",
            "       ██       ",
            "    ████████    ",
            "       ██       ",
            "  ████████████  ",
            "       ██       ",
            "       ██       ",
            "    ████████    ",
            "   ██      ██   ",
            "  ██        ██  ",
            " ██          ██ ",
            "██            ██",
            "                "
        },
        { // 2. Cybernetic Digital Eye Symbol
            "      ████      ",
            "    ██    ██    ",
            "   ██  ██  ██   ",
            "  ██  ████  ██  ",
            " ██  ██████  ██ ",
            "██  ████████  ██",
            "██  ██ ── ██  ██",
            "██  ██    ██  ██",
            "██  ████████  ██",
            " ██  ██████  ██ ",
            "  ██  ████  ██  ",
            "   ██  ██  ██   ",
            "    ██    ██    ",
            "      ████      ",
            "                ",
            "                "
        },
        { // 3. Cybernetic Skull / Biohazard Fractal Glyph
            "    ████████    ",
            "   ██  ██  ██   ",
            "  ██   ██   ██  ",
            "  ██   ██   ██  ",
            "   ██  ██  ██   ",
            "    ████████    ",
            "  ██   ██   ██  ",
            " ██    ██    ██ ",
            "██     ██     ██",
            " ██    ██    ██ ",
            "  ██   ██   ██  ",
            "    ████████    ",
            "   ██  ██  ██   ",
            "  ██   ██   ██  ",
            "   ██  ██  ██   ",
            "    ████████    "
        }
    };

    // Solid target pixels within each symbol to zoom into
    private static final int[][] TARGET_PIXELS = {
        { 8, 3 },  // Center horizontal bar of 'ｦ'
        { 5, 5 },  // Inner circle of cybernetic eye
        { 8, 7 }   // Solid center hub of infinity glyph
    };

    private static final String RUNES = "ｦｧｨｩｪｫｬｭｮｯｰｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ1234567890*+-";

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
        
        // Allocate falling rain streams for all columns
        this.ambientHeads = new double[width];
        this.ambientSpeeds = new double[width];
        for (int i = 0; i < width; i++) {
            ambientHeads[i] = random.nextInt(height + 20) - 25;
            ambientSpeeds[i] = 7.0 + random.nextDouble() * 12.0;
        }
    }

    @Override
    public void update(double time, double deltaTime) {
        this.time = time;

        // Physics update for falling digital rain streams
        for (int i = 0; i < width; i++) {
            ambientHeads[i] += ambientSpeeds[i] * deltaTime;
            if (ambientHeads[i] - 18 > height) {
                // Wrap stream back to top with randomized initial offsets
                ambientHeads[i] = -random.nextInt(25) - 5;
                ambientSpeeds[i] = 7.0 + random.nextDouble() * 12.0;
            }
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        // State loop: 12 seconds total cycle duration
        double cycleDuration = 12.0;
        double cycleTime = time % cycleDuration;
        int symbolIndex = (int) ((time / cycleDuration) % SYMBOLS.length);

        String[] symbolGrid = SYMBOLS[symbolIndex];
        int[] targetPixel = TARGET_PIXELS[symbolIndex];

        double zoom = 1.0;
        double symbolOpacity = 0.0;
        double zoomFactor = 0.0;

        // Phase control logic
        if (cycleTime < 3.5) {
            // Phase 1: Classic ambient rain pause (as it started)
            zoom = 1.0;
            symbolOpacity = 0.0;
        } else if (cycleTime < 7.0) {
            // Phase 2: Form Bitmap (smoothly fade in the central symbol)
            zoom = 1.0;
            double t = (cycleTime - 3.5) / 3.5;
            symbolOpacity = 0.5 - 0.5 * Math.cos(t * Math.PI); // Ease-in-out curve
        } else {
            // Phase 3: Zoom In (exponentially fly inside the target pixel)
            symbolOpacity = 1.0;
            zoomFactor = (cycleTime - 7.0) / 5.0; // 0.0 to 1.0
            
            // Hyper-exponential zoom glide
            zoom = 1.0 + Math.pow(zoomFactor, 5) * 150.0;
        }

        // Camera panning: smoothly center on target pixel coordinates
        double targetX = targetPixel[0];
        double targetY = targetPixel[1];
        double zoomCenterX = 8.0;
        double zoomCenterY = 8.0;

        if (cycleTime >= 7.0) {
            // Smoothly pan camera from symbol center (8, 8) to target coordinate (targetX, targetY)
            double panT = 0.5 - 0.5 * Math.cos(zoomFactor * Math.PI); // Smoothstep pan
            zoomCenterX = 8.0 + (targetX - 8.0) * panT;
            zoomCenterY = 8.0 + (targetY - 8.0) * panT;
        }

        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double aspect = 2.0; // Terminal grid cell vertical-to-horizontal character aspect ratio

        // Render each grid cell sequentially
        for (int y = 0; y < height; y++) {
            double dy = (y - centerY) * aspect;

            for (int x = 0; x < width; x++) {
                double dx = x - centerX;

                // Inverse camera transformation: Map screen coordinates to symbol grid space
                double u = dx / zoom + zoomCenterX;
                double v = dy / zoom + zoomCenterY;

                int iu = (int) Math.floor(u);
                int iv = (int) Math.floor(v);

                boolean isInsideSymbol = false;
                if (iu >= 0 && iu < 16 && iv >= 0 && iv < 16) {
                    if (symbolGrid[iv].charAt(iu) == '█') {
                        isInsideSymbol = true;
                    }
                }

                // Check falling rain telemetry for the current column
                double headY = ambientHeads[x];
                boolean hasRain = (y <= headY && y >= headY - 16);

                int fgColor = 0x000000;
                int bgColor = 0x000000;
                char glyph = ' ';

                if (hasRain) {
                    double age = (headY - y) / 16.0;

                    // Dynamically morphing Katakana/Rune character selection
                    int charSeed = x * 73 + y * 31 + (int) (time * 12.0);
                    glyph = RUNES.charAt(Math.abs(charSeed) % RUNES.length());

                    // Linear falling rain brightness values
                    int dimG = (int) (80 * (1.0 - age) + 15);   // 15 to 95 (ambient)

                    if (isInsideSymbol && symbolOpacity > 0.0) {
                        // High-brightness highlight for the symbol!
                        double currentOpacity = symbolOpacity;

                        // Smoothly fade the symbol's brightness down back to ambient rain levels at the end of the zoom.
                        // Since every cell is zoomed-in (inside the pixel), this results in a perfectly seamless loop reset.
                        if (cycleTime >= 7.0 && zoomFactor > 0.8) {
                            double fadeOut = (1.0 - zoomFactor) / 0.2; // 1.0 -> 0.0
                            currentOpacity = fadeOut;
                        }

                        int r, g, b;
                        if (age < 0.06) {
                            // Glowing white head
                            r = (int) (255 * currentOpacity + 136 * (1.0 - currentOpacity));
                            g = 255;
                            b = (int) (255 * currentOpacity + 136 * (1.0 - currentOpacity));
                        } else {
                            // Glowing bright green vs dim background green
                            r = 0;
                            g = (int) (255 * currentOpacity + dimG * (1.0 - currentOpacity));
                            b = 0;
                        }
                        fgColor = (r << 16) | (g << 8) | b;

                        // Subtle dark green cell backing inside the symbol for solid legibility
                        int bgG = (int) (35 * currentOpacity);
                        bgColor = bgG << 8;
                    } else {
                        // Faint ambient background rain
                        if (age < 0.06) {
                            fgColor = 0x88FF88; // Slightly brighter leading head
                        } else {
                            fgColor = dimG << 8;
                        }
                        bgColor = 0x000000;
                    }
                } else {
                    glyph = ' ';
                    fgColor = 0x000000;
                    bgColor = 0x000000;
                }

                // Write the cell to our true-color buffer
                canvas.writeCell(x, y, glyph, fgColor, bgColor);
            }
        }
    }

    @Override
    public String getName() {
        return "Matrix Cinematic Symbol Zoom";
    }
}
