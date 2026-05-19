package fastterminal.demoscene.effects;

import fastterminal.FastTerminalScene;
import fastterminal.demoscene.DemosceneEffect;

/**
 * @class IntroEffect
 * @brief High-frequency OLED sine color wave background renderer.
 * 
 * Computes individual character cell background and foreground HSL-like gradients
 * dynamically via math phase transitions.
 */
public class IntroEffect implements DemosceneEffect {

    private int width;
    private int height;
    private double phase = 0.0;

    /**
     * @brief Initializes dimensions for the effect.
     * @param width Terminal screen width in columns.
     * @param height Terminal screen height in rows.
     */
    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * @brief Advances gradient wave phase transitions.
     * @param time Total elapsed time in seconds.
     * @param deltaTime Elapsed time in seconds since last frame.
     */
    @Override
    public void update(double time, double deltaTime) {
        phase = time * 18.0;
    }

    /**
     * @brief Renders the color wave cells directly to the rendering canvas.
     * @param canvas Double-buffer render target screen.
     */
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
    }

    /**
     * @brief Returns the visual user-friendly name of the effect.
     * @return String effect name label.
     */
    @Override
    public String getName() {
        return "OLED Color Wave";
    }
}
