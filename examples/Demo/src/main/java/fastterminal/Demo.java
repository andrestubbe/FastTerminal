package fastterminal;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

/**
 * Stunning 24-bit True-Color & Unicode Emoji Fullscreen Scalable Demo.
 * Demonstrates high-performance state-optimized ANSI rendering on a dynamic viewport.
 */
public class Demo {

    public static void main(String[] args) {
        System.out.println("Initializing FastTerminal Fullscreen Demo...");

        int cols = 80;
        int rows = 24;

        Terminal jlineTerminal = null;
        try {
            // Detect terminal dimensions using JLine
            jlineTerminal = TerminalBuilder.builder().system(true).build();
            cols = jlineTerminal.getWidth();
            rows = jlineTerminal.getHeight();
            // Fallback for empty IDE runs
            if (cols <= 0) cols = 80;
            if (rows <= 0) rows = 24;
        } catch (IOException e) {
            System.err.println("JLine terminal detection failed, falling back to 80x24: " + e.getMessage());
        }

        System.out.printf("Detected Terminal Geometry: %d Columns x %d Rows\n", cols, rows);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // Enter Fullscreen Alternate Screen Buffer, Hide Cursor
        System.out.print("\033[?1049h\033[?25l");
        System.out.flush();

        // Create the high-performance renderer
        TerminalRenderer renderer = new TerminalRenderer(cols, rows);

        // Add a fullscreen canvas scene
        final int finalCols = cols;
        final int finalRows = rows;
        TerminalScene canvas = new TerminalScene(0, 0, cols, rows);
        
        final double[] phase = { 0.0 };
        canvas.setUpdater(() -> {
            phase[0] += 0.15; // Shift wave speed
            
            // 1. Fill the background grid with a dynamic 24-bit RGB sine wave pattern
            for (int r = 0; r < finalRows; r++) {
                for (int c = 0; c < finalCols; c++) {
                    // Compute wave colors based on coordinate grid and animation phase
                    double freq = 0.1;
                    int fgRed = (int) (Math.sin(freq * c + phase[0]) * 127 + 128);
                    int fgGreen = (int) (Math.sin(freq * c + phase[0] + 2.0 * Math.PI / 3.0) * 127 + 128);
                    int fgBlue = (int) (Math.sin(freq * c + phase[0] + 4.0 * Math.PI / 3.0) * 127 + 128);
                    int fgColor = (fgRed << 16) | (fgGreen << 8) | fgBlue;

                    // Contrasting background
                    int bgRed = (int) (Math.sin(freq * r - phase[0]) * 40 + 45);
                    int bgGreen = (int) (Math.sin(freq * r - phase[0] + Math.PI) * 40 + 45);
                    int bgBlue = 30;
                    int bgColor = (bgRed << 16) | (bgGreen << 8) | bgBlue;

                    // Alternate character patterns to show perfect alignment
                    int codepoint = 'X';
                    if ((c + r) % 2 == 0) {
                        codepoint = '░';
                    }

                    canvas.writeCell(c, r, codepoint, fgColor, bgColor);
                }
            }

            // 2. Draw moving Emojis to prove UTF-32 unicode-safe length mapping
            int emojiX1 = (int) ((Math.sin(phase[0] * 0.5) * 0.4 + 0.5) * finalCols);
            int emojiY1 = (int) ((Math.cos(phase[0] * 0.5) * 0.3 + 0.5) * finalRows);
            canvas.writeString(emojiX1, emojiY1, "🚀", 0xFFFFFF, -1);

            int emojiX2 = (int) ((Math.cos(phase[0] * 0.7 + 1.0) * 0.4 + 0.5) * finalCols);
            int emojiY2 = (int) ((Math.sin(phase[0] * 0.7 + 1.0) * 0.3 + 0.5) * finalRows);
            canvas.writeString(emojiX2, emojiY2, "🌈", 0xFFFFFF, -1);
            
            int emojiX3 = (int) ((Math.sin(phase[0] * 0.3 + 2.0) * 0.4 + 0.5) * finalCols);
            int emojiY3 = (int) ((Math.cos(phase[0] * 0.3 + 2.0) * 0.3 + 0.5) * finalRows);
            canvas.writeString(emojiX3, emojiY3, "⚡", 0xFFFF00, -1);

            // 3. Render a premium centered Title Card with solid contrast background
            String titleText = " ⚡ FASTTERMINAL TRUE-COLOR ⚡ ";
            int titleX = Math.max(0, (finalCols - titleText.length()) / 2);
            int titleY = finalRows / 2;
            canvas.writeString(titleX, titleY, titleText, 0x000000, 0xFFCC00);

            String subtitle = " Zero-Dependency | 60 FPS | Codepoint Safe ";
            int subX = Math.max(0, (finalCols - subtitle.length()) / 2);
            canvas.writeString(subX, titleY + 1, subtitle, 0xFFFFFF, 0x222222);
        });

        renderer.addScene(canvas);

        // Run the dynamic OLED wave rendering loop (e.g. 300 frames, ~5 seconds)
        long frameTimeMs = 1000 / 60; // Locked 60 FPS target
        for (int frame = 0; frame < 300; frame++) {
            long startTime = System.currentTimeMillis();

            canvas.setDirty(true);
            renderer.render();

            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = frameTimeMs - elapsed;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {}
            }
        }

        // Restore alternate screen buffer, restore cursor
        System.out.print("\033[?1049l\033[?25h");
        System.out.flush();

        if (jlineTerminal != null) {
            try {
                jlineTerminal.close();
            } catch (IOException ignored) {}
        }

        System.out.println("=== FastTerminal Demo Finished Safely ===");
    }
}
