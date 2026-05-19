package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * 🌊 Wavefall Harmonic Line Streams effect.
 * Renders multiple interwoven sine waves flowing vertically down with additive neon glowing overlays.
 */
public class WavefallEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double time = 0.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        time = frameIndex * 0.06;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        // Clear canvas with a very soft deep navy fade
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                canvas.writeCell(x, y, ' ', 0, 0x01010A);
            }
        }

        // Draw three overlapping sinus waves
        drawWave(canvas, 1.0, 1.3, 0.0, 0xEC4899, "█"); // Pink wave
        drawWave(canvas, 1.5, 0.9, 1.5, 0x06B6D4, "▓"); // Cyan wave
        drawWave(canvas, 0.8, 1.7, 3.0, 0x8B5CF6, "▒"); // Violet wave
    }

    private void drawWave(FastTerminalScene canvas, double speed, double freq, double phaseOffset, int color, String glyphChar) {
        char glyph = glyphChar.charAt(0);
        double centerY = height / 2.0;

        for (int x = 0; x < width; x++) {
            // Harmonic wave equation
            double sinVal = Math.sin(x * 0.08 * freq + time * speed + phaseOffset)
                          + 0.4 * Math.sin(x * 0.16 * freq - time * speed * 1.5);
            
            double targetY = centerY + (height * 0.35) * sinVal;

            int yVal = (int) Math.round(targetY);
            if (yVal >= 0 && yVal < height) {
                // Write primary line node
                canvas.writeCell(x, yVal, glyph, color, 0x02000A);

                // Add vertical glowing halos
                if (yVal + 1 < height) {
                    canvas.writeCell(x, yVal + 1, '▒', fadeColor(color, 0.4), 0x010005);
                }
                if (yVal - 1 >= 0) {
                    canvas.writeCell(x, yVal - 1, '▒', fadeColor(color, 0.4), 0x010005);
                }
                if (yVal + 2 < height) {
                    canvas.writeCell(x, yVal + 2, '░', fadeColor(color, 0.15), 0x010002);
                }
                if (yVal - 2 >= 0) {
                    canvas.writeCell(x, yVal - 2, '░', fadeColor(color, 0.15), 0x010002);
                }
            }
        }
    }

    private int fadeColor(int color, double multiplier) {
        int r = (int) (((color >> 16) & 0xFF) * multiplier);
        int g = (int) (((color >> 8) & 0xFF) * multiplier);
        int b = (int) ((color & 0xFF) * multiplier);
        return (r << 16) | (g << 8) | b;
    }

    @Override
    public String getName() {
        return "🌊 Wavefall Harmonic Line Streams";
    }
}
