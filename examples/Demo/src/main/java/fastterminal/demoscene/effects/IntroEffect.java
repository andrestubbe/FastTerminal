package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * ⚡ The iconic introductory OLED Wave & Floating Emojis demo.
 * Renders high-performance dynamic color waves and animated emojis (🚀, 🌈, ⚡).
 */
public class IntroEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double phase = 0.0;

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(long frameIndex) {
        phase = frameIndex * 0.15;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        canvas.clear();

        // 1. Render background OLED wave
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                double freq = 0.1;
                int fgRed = (int) (Math.sin(freq * c + phase) * 127 + 128);
                int fgGreen = (int) (Math.sin(freq * c + phase + 2.0 * Math.PI / 3.0) * 127 + 128);
                int fgBlue = (int) (Math.sin(freq * c + phase + 4.0 * Math.PI / 3.0) * 127 + 128);
                int fgColor = (fgRed << 16) | (fgGreen << 8) | fgBlue;

                int bgRed = (int) (Math.sin(freq * r - phase) * 40 + 45);
                int bgGreen = (int) (Math.sin(freq * r - phase + Math.PI) * 40 + 45);
                int bgBlue = 30;
                int bgColor = (bgRed << 16) | (bgGreen << 8) | bgBlue;

                int codepoint = 'X';
                if ((c + r) % 2 == 0) {
                    codepoint = '░';
                }
                canvas.writeCell(c, r, codepoint, fgColor, bgColor);
            }
        }

        // 2. Render moving emojis
        int titleY = height / 2;

        // Upper limit row bounds
        int upperLimit = Math.max(1, titleY - 2);
        int emojiX1 = (int) ((Math.sin(phase * 0.5) * 0.4 + 0.5) * width);
        int emojiY1 = (int) ((Math.cos(phase * 0.5) * 0.4 + 0.5) * upperLimit);
        int rY = Math.min(emojiY1, upperLimit);
        if (emojiX1 >= 0 && emojiX1 < width - 1 && rY >= 0 && rY < height) {
            canvas.writeString(emojiX1, rY, "🚀", 0xFFFFFF, -1);
            canvas.writeCell(emojiX1 + 1, rY, -99, -1, -1); // Clear emoji overlap cell!
        }

        // Lower limit row bounds
        int lowerLimitStart = Math.min(height - 1, titleY + 2);
        int lowerLimitRange = Math.max(1, height - 1 - lowerLimitStart);
        int emojiX2 = (int) ((Math.cos(phase * 0.7 + 1.0) * 0.4 + 0.5) * width);
        int emojiY2 = lowerLimitStart + (int) ((Math.sin(phase * 0.7 + 1.0) * 0.4 + 0.5) * lowerLimitRange);
        int lY = Math.min(emojiY2, height - 1);
        if (emojiX2 >= 0 && emojiX2 < width - 1 && lY >= 0 && lY < height) {
            canvas.writeString(emojiX2, lY, "🌈", 0xFFFFFF, -1);
            canvas.writeCell(emojiX2 + 1, lY, -99, -1, -1); // Clear emoji overlap cell!
        }

        // Lightning bolt near the top edge
        int emojiX3 = (int) ((Math.sin(phase * 0.3 + 2.0) * 0.4 + 0.5) * width);
        if (emojiX3 >= 0 && emojiX3 < width - 1) {
            canvas.writeString(emojiX3, 0, "⚡", 0xFFFF00, -1);
            canvas.writeCell(emojiX3 + 1, 0, -99, -1, -1); // Clear emoji overlap cell!
        }
    }

    @Override
    public String getName() {
        return "🌈 Introductory OLED Wave & Floating Emojis";
    }
}
