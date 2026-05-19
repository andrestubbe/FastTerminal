package fastterminal;

/**
 * Stunning 24-bit True-Color & Unicode Emoji Fullscreen Scalable Demo.
 * Demonstrates high-performance state-optimized ANSI rendering on a dynamic
 * viewport.
 * Runs indefinitely with live native JNI resizing and rock-solid title
 * positioning.
 */
public class Demo {

    public static void main(String[] args) {
        System.out.println("Initializing FastTerminal Live Fullscreen Demo...");

        // Initial default sizes
        int cols = 120;
        int rows = 30;

        // Try to query starting size natively
        try {
            int[] size = FastTerminal.getTerminalSize();
            System.out.println("[DEBUG] Native Console Sizing: " + (size != null ? size[0] + "x" + size[1] : "NULL"));
            if (size != null && size[0] > 0 && size[1] > 0) {
                cols = size[0];
                rows = size[1];
            }
        } catch (Throwable e) {
            System.err.println("[DEBUG] JNI Sizing Failed: " + e.getMessage());
            e.printStackTrace();
        }

        // Enter Fullscreen Alternate Screen Buffer, Hide Cursor
        System.out.print(fastansi.FastANSI.ALT_BUFFER_ON + fastansi.FastANSI.CURSOR_HIDE);

        FastTerminalRenderer renderer = new FastTerminalRenderer(cols, rows);
        FastTerminalScene canvas = new FastTerminalScene(0, 0, cols, rows);
        renderer.addScene(canvas);

        final double[] phase = { 0.0 };
        long frameTimeMs = 1000 / 120; // Target locked 120 FPS

        // Loop runs indefinitely until user interrupts (e.g. via Ctrl+C)
        while (true) {
            long startTime = System.currentTimeMillis();

            // 1. LIVE RESIZE DETECTION (Natively via JNI, fully garbage-free!)
            int[] size = FastTerminal.getWindowSize(cols, rows);
            if (renderer.resize(size[0], size[1])) {
                cols = size[0];
                rows = size[1];
                canvas.resize(cols, rows);
            }

            final int finalCols = cols;
            final int finalRows = rows;

            // 2. UPDATE VIEWPORT GRID
            canvas.clear();
            phase[0] += 0.15; // Animation speed

            // Render background OLED wave
            for (int r = 0; r < finalRows; r++) {
                for (int c = 0; c < finalCols; c++) {
                    double freq = 0.1;
                    int fgRed = (int) (Math.sin(freq * c + phase[0]) * 127 + 128);
                    int fgGreen = (int) (Math.sin(freq * c + phase[0] + 2.0 * Math.PI / 3.0) * 127 + 128);
                    int fgBlue = (int) (Math.sin(freq * c + phase[0] + 4.0 * Math.PI / 3.0) * 127 + 128);
                    int fgColor = (fgRed << 16) | (fgGreen << 8) | fgBlue;

                    int bgRed = (int) (Math.sin(freq * r - phase[0]) * 40 + 45);
                    int bgGreen = (int) (Math.sin(freq * r - phase[0] + Math.PI) * 40 + 45);
                    int bgBlue = 30;
                    int bgColor = (bgRed << 16) | (bgGreen << 8) | bgBlue;

                    int codepoint = 'X';
                    if ((c + r) % 2 == 0) {
                        codepoint = '░';
                    }
                    canvas.writeCell(c, r, codepoint, fgColor, bgColor);
                }
            }

            // 3. PREVENT LAYOUT JITTER & CLEAR EMOJI OVERLAYS
            // Title is centered at row titleY. We restrict moving emojis to float ONLY
            // in the upper/lower boundary sections so they never enter the title's line.
            int titleY = finalRows / 2;

            // Upper limit row bounds [1, titleY - 2]
            int upperLimit = Math.max(1, titleY - 2);
            int emojiX1 = (int) ((Math.sin(phase[0] * 0.5) * 0.4 + 0.5) * finalCols);
            int emojiY1 = (int) ((Math.cos(phase[0] * 0.5) * 0.4 + 0.5) * upperLimit);
            int rY = Math.min(emojiY1, upperLimit);
            canvas.writeString(emojiX1, rY, "🚀", 0xFFFFFF, -1);
            canvas.writeCell(emojiX1 + 1, rY, -99, -1, -1); // Clear emoji overlap cell!

            // Lower limit row bounds [titleY + 2, finalRows - 1]
            int lowerLimitStart = Math.min(finalRows - 1, titleY + 2);
            int lowerLimitRange = Math.max(1, finalRows - 1 - lowerLimitStart);
            int emojiX2 = (int) ((Math.cos(phase[0] * 0.7 + 1.0) * 0.4 + 0.5) * finalCols);
            int emojiY2 = lowerLimitStart + (int) ((Math.sin(phase[0] * 0.7 + 1.0) * 0.4 + 0.5) * lowerLimitRange);
            int lY = Math.min(emojiY2, finalRows - 1);
            canvas.writeString(emojiX2, lY, "🌈", 0xFFFFFF, -1);
            canvas.writeCell(emojiX2 + 1, lY, -99, -1, -1); // Clear emoji overlap cell!

            // Lightning bolt floats near the top edge
            int emojiX3 = (int) ((Math.sin(phase[0] * 0.3 + 2.0) * 0.4 + 0.5) * finalCols);
            canvas.writeString(emojiX3, 0, "⚡", 0xFFFF00, -1);
            canvas.writeCell(emojiX3 + 1, 0, -99, -1, -1); // Clear emoji overlap cell!

            // 4. RENDER STATIC SOLID TITLE CARDS (Adapts dynamically to window size to prevent noise!)
            if (finalCols >= 15 && finalRows >= 5) {
                String titleText = finalCols >= 32 ? " [ FASTTERMINAL TRUE-COLOR ] " : " [ FAST ] ";
                int titleX = (finalCols - titleText.length()) / 2;
                canvas.writeString(titleX, titleY, titleText, 0x000000, 0xFFCC00);

                if (finalCols >= 54 && finalRows >= 8) {
                    String subtitle = " Zero-Dependency | Locked 120 FPS | Dynamic Resize ";
                    int subX = (finalCols - subtitle.length()) / 2;
                    canvas.writeString(subX, Math.min(titleY + 1, finalRows - 1), subtitle, 0xFFFFFF, 0x222222);
                }
            }

            // 5. COMBO BLIT
            canvas.setDirty(true);
            renderer.render();

            // FPS limiter calculation
            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = frameTimeMs - elapsed;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
