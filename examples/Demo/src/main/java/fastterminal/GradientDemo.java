package fastterminal;

/**
 * Premium showcase for FastTerminal's 24-bit True Color gradients.
 * Features an animated fluid diagonal gradient background and a floating neon card.
 */
public final class GradientDemo {

    public static void main(String[] args) {
        System.out.println("Initializing FastTerminal 24-bit Gradient Demo...");

        // Shutdown hook to cleanly restore terminal settings
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Ansi.print(Ansi.EXIT_ALT_BUFFER, Ansi.SHOW_CURSOR, Ansi.RESET);
        }));

        // Enter Alternate Buffer, Hide Cursor
        Ansi.print(Ansi.ENTER_ALT_BUFFER, Ansi.HIDE_CURSOR);

        int cols = 80;
        int rows = 30;

        try {
            int[] size = FastTerminal.getTerminalSize();
            if (size != null && size[0] > 0 && size[1] > 0) {
                cols = size[0];
                rows = size[1];
            }
        } catch (Throwable ignored) {}

        FastTerminalRenderer renderer = new FastTerminalRenderer(cols, rows);
        FastTerminalScene canvas = new FastTerminalScene(0, 0, cols, rows);
        renderer.addScene(canvas);

        double phase = 0.0;
        long frameTimeMs = 1000 / 120; // 120 FPS

        while (true) {
            long startTime = System.currentTimeMillis();

            // 1. LIVE RESIZE DETECTION (In-place & fully Garbage-Free!)
            int[] size = FastTerminal.getWindowSize(cols, rows);
            if (renderer.resize(size[0], size[1])) {
                cols = size[0];
                rows = size[1];
                canvas.resize(cols, rows);
            }

            canvas.clear();

            // 2. GENERATE ANIMATED FLUID DIAGONAL GRADIENT BACKGROUND
            // Interpolate colors dynamically based on sine waves!
            double t1 = (Math.sin(phase) + 1.0) / 2.0;
            double t2 = (Math.cos(phase * 0.8) + 1.0) / 2.0;

            // Violet (0x8B5CF6) -> Hot Pink (0xEC4899)
            int bgStart = Gradient.interpolate(0x6366F1, 0xD946EF, t1);
            // Deep Cyan (0x0891B2) -> Emerald Green (0x10B981)
            int bgEnd = Gradient.interpolate(0x06B6D4, 0x14B8A6, t2);

            Gradient.applyDiagonalBg(canvas, 0, 0, cols, rows, bgStart, bgEnd);

            // 3. RENDER STATIC SOLID TITLE CARDS (Adapts dynamically to window size to prevent noise!)
            final int finalCols = cols;
            final int finalRows = rows;
            int titleY = finalRows / 2 - 1;

            if (finalCols >= 15 && finalRows >= 5) {
                String titleText = finalCols >= 36 ? " [ FASTTERMINAL 24-BIT GRADIENTS ] " : " [ GRADIENTS ] ";
                int titleX = (finalCols - titleText.length()) / 2;
                canvas.writeString(titleX, titleY, titleText, 0x000000, 0xFFCC00);

                if (finalCols >= 50 && finalRows >= 8) {
                    String subtitle = " Buttery-Smooth 120 FPS | Zero-GC Swapchain ";
                    int subX = (finalCols - subtitle.length()) / 2;
                    canvas.writeString(subX, Math.min(titleY + 1, finalRows - 1), subtitle, 0xFFFFFF, 0x222222);
                }
            }

            // 4. FLUSH
            canvas.setDirty(true);
            renderer.render();

            phase += 0.02; // Update animation phase

            // Frame Rate Regulator
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed < frameTimeMs) {
                try {
                    Thread.sleep(frameTimeMs - elapsed);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
