package fastterminal;

/**
 * High-Performance "Fake UI" Dynamic Dashboard Demo.
 * Renders a glowing, animated, retro-futuristic telemetry control panel
 * centered inside the console, scaling dynamically with native JNI resize events.
 */
public class UIDemo {

    public static void main(String[] args) {
        System.out.println("Initializing FastTerminal Fake UI Dashboard Demo...");

        // Register JVM Shutdown Hook to safely restore the terminal buffer on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Restore normal screen buffer, show cursor, and reset colors
            Ansi.print(Ansi.EXIT_ALT_BUFFER, Ansi.SHOW_CURSOR, Ansi.RESET);
        }));

        // Enter Alternate Screen Buffer, Hide Cursor
        Ansi.print(Ansi.ENTER_ALT_BUFFER, Ansi.HIDE_CURSOR);

        int cols = 80;
        int rows = 30;

        // Try to query starting size natively
        try {
            int[] size = FastTerminal.getTerminalSize();
            if (size != null && size[0] > 0 && size[1] > 0) {
                cols = size[0];
                rows = size[1];
            }
        } catch (Throwable ignored) {}

        FastTerminalRenderer renderer = null;
        FastTerminalScene canvas = null;

        double phase = 0.0;
        long frameTimeMs = 1000 / 120; // Locked 120 FPS target

        while (true) {
            long startTime = System.currentTimeMillis();

            // 1. DYNAMIC RESIZE DETECTION via Win32 JNI
            int currentCols = cols;
            int currentRows = rows;
            try {
                int[] size = FastTerminal.getTerminalSize();
                if (size != null && size[0] > 0 && size[1] > 0) {
                    currentCols = size[0];
                    currentRows = size[1];
                }
            } catch (Throwable ignored) {}

            // Recreate viewport scene if resized
            if (renderer == null || canvas == null || currentCols != cols || currentRows != rows) {
                cols = currentCols;
                rows = currentRows;
                renderer = new FastTerminalRenderer(cols, rows);
                canvas = new FastTerminalScene(0, 0, cols, rows);
                renderer.addScene(canvas);
            }

            canvas.clear();
            phase += 0.05; // Animation step

            // 2. RENDER DEEP SPACE BACKGROUND GRID
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int bgVal = 0x05070A; // Ultra-dark obsidian blue
                    int codepoint = ' ';
                    int fgVal = 0x1E293B; // Slate gray

                    // Faint grid background dots
                    if (c % 10 == 0 && r % 5 == 0) {
                        codepoint = '·';
                    }
                    canvas.writeCell(c, r, codepoint, fgVal, bgVal);
                }
            }

            // 3. FAKE WINDOW SPECIFICATION (Centered)
            int winW = 60;
            int winH = 20;

            // Make sure the window fits inside the viewport
            if (winW > cols) winW = cols - 2;
            if (winH > rows) winH = rows - 2;

            if (winW > 10 && winH > 5) {
                int startX = (cols - winW) / 2;
                int startY = (rows - winH) / 2;

                // 4. DRAW SLEEK BACKGROUND SHADOW & SLEEK WINDOW PANEL
                for (int r = startY; r < startY + winH; r++) {
                    for (int c = startX; c < startX + winW; c++) {
                        // Sleek transparent slate background for our window pane
                        canvas.writeCell(c, r, ' ', 0xFFFFFF, 0x0E1726);
                    }
                }

                // 5. ANIMATED NEON BORDER (Cycling HSL Gradient)
                for (int c = startX + 1; c < startX + winW - 1; c++) {
                    // Top border
                    int topColor = getNeonColor(phase + c * 0.04);
                    canvas.writeCell(c, startY, '─', topColor, 0x0E1726);
                    // Bottom border
                    int botColor = getNeonColor(phase + c * 0.04 + 1.5);
                    canvas.writeCell(c, startY + winH - 1, '─', botColor, 0x0E1726);
                }
                for (int r = startY + 1; r < startY + winH - 1; r++) {
                    // Left border
                    int leftColor = getNeonColor(phase + r * 0.06 + 0.5);
                    canvas.writeCell(startX, r, '│', leftColor, 0x0E1726);
                    // Right border
                    int rightColor = getNeonColor(phase + r * 0.06 + 2.0);
                    canvas.writeCell(startX + winW - 1, r, '│', rightColor, 0x0E1726);
                }

                // Corners
                canvas.writeCell(startX, startY, '┌', getNeonColor(phase), 0x0E1726);
                canvas.writeCell(startX + winW - 1, startY, '┐', getNeonColor(phase + 1.0), 0x0E1726);
                canvas.writeCell(startX, startY + winH - 1, '└', getNeonColor(phase + 2.0), 0x0E1726);
                canvas.writeCell(startX + winW - 1, startY + winH - 1, '┘', getNeonColor(phase + 3.0), 0x0E1726);

                // 6. SOLID BANNER WINDOW TITLE BAR
                for (int c = startX + 1; c < startX + winW - 1; c++) {
                    canvas.writeCell(c, startY + 1, ' ', 0xFFFFFF, 0x1E1B4B); // Dark indigo background
                }
                // Mock Mac OS Control Buttons
                canvas.writeString(startX + 2, startY + 1, "●", 0xEF4444, 0x1E1B4B); // Close (Red)
                canvas.writeString(startX + 4, startY + 1, "●", 0xF59E0B, 0x1E1B4B); // Minimize (Yellow)
                canvas.writeString(startX + 6, startY + 1, "●", 0x10B981, 0x1E1B4B); // Maximize (Green)

                // Title string centered safely with -99 wide-character continuation cells!
                // Total visual width is 29 columns.
                int titleX = startX + (winW - 29) / 2;
                canvas.writeCell(titleX, startY + 1, ' ', 0xF9FAFB, 0x1E1B4B);
                canvas.writeCell(titleX + 1, startY + 1, 0x26A1, 0xFBBF24, 0x1E1B4B); // ⚡ (Lightning bolt codepoint)
                canvas.writeCell(titleX + 2, startY + 1, -99, -1, -1); // Emoji continuation cell!
                canvas.writeString(titleX + 3, startY + 1, " NEURAL LINK TELEMETRY ", 0xF9FAFB, 0x1E1B4B);
                canvas.writeCell(titleX + 26, startY + 1, 0x26A1, 0xFBBF24, 0x1E1B4B); // ⚡ (Lightning bolt codepoint)
                canvas.writeCell(titleX + 27, startY + 1, -99, -1, -1); // Emoji continuation cell!
                canvas.writeCell(titleX + 28, startY + 1, ' ', 0xF9FAFB, 0x1E1B4B);

                // 7. LEFT SIDEBAR PANEL (System Diagnostics)
                int sideStartX = startX + 2;
                int sideWidth = 16;
                canvas.writeString(sideStartX, startY + 3, " [ DIAGNOSTICS ] ", 0x60A5FA, 0x0E1726);
                
                // Telemetry status
                boolean blinkOn = (int)(phase * 2) % 2 == 0;
                canvas.writeString(sideStartX, startY + 5, blinkOn ? "● CORE ON" : "  CORE ON", 0x10B981, 0x0E1726);
                canvas.writeString(sideStartX, startY + 6, "NODE: SECURE", 0xF59E0B, 0x0E1726);

                // Dynamic Progress Indicators
                int cpuUsage = 50 + (int)(Math.sin(phase * 1.5) * 20);
                canvas.writeString(sideStartX, startY + 8, "CPU: " + cpuUsage + "%", 0xE2E8F0, 0x0E1726);
                drawProgressBar(canvas, sideStartX, startY + 9, cpuUsage, 12, 0xEF4444, 0x0E1726);

                int ramUsage = 80 + (int)(Math.cos(phase * 0.8) * 5);
                canvas.writeString(sideStartX, startY + 11, "RAM: " + ramUsage + "%", 0xE2E8F0, 0x0E1726);
                drawProgressBar(canvas, sideStartX, startY + 12, ramUsage, 12, 0x3B82F6, 0x0E1726);

                canvas.writeString(sideStartX, startY + 14, "RTT: 14 ms", 0x10B981, 0x0E1726);
                canvas.writeString(sideStartX, startY + 15, "FPS: 120", 0x10B981, 0x0E1726);

                // Separator Line
                for (int r = startY + 3; r < startY + winH - 2; r++) {
                    canvas.writeCell(startX + sideWidth + 3, r, '│', 0x334155, 0x0E1726);
                }

                // 8. RIGHT CONTENT PANEL (Animated Wave Graph)
                int graphStartX = startX + sideWidth + 5;
                int graphW = winW - sideWidth - 8;
                int graphH = 8;
                int graphY = startY + 3;

                canvas.writeString(graphStartX, graphY, " [ TRANSMISSION STREAM ] ", 0xFBBF24, 0x0E1726);

                // Draw bounding box for graphs
                for (int c = graphStartX; c < graphStartX + graphW; c++) {
                    canvas.writeCell(c, graphY + 1, '─', 0x1E293B, 0x0E1726);
                    canvas.writeCell(c, graphY + graphH + 2, '─', 0x1E293B, 0x0E1726);
                }

                // Plot animated sine wave inside graph bounding box
                for (int c = 0; c < graphW; c++) {
                    double waveVal = Math.sin(c * 0.25 - phase * 2.0);
                    int relativeRow = (int) ((waveVal + 1.0) * 0.5 * (graphH - 1));
                    int absoluteC = graphStartX + c;
                    int absoluteR = graphY + 2 + relativeRow;

                    // Plot neon green dot
                    canvas.writeCell(absoluteC, absoluteR, '♦', 0x22C55E, 0x0E1726);
                }

                // Console dynamic logs below graph
                int logY = graphY + graphH + 4;
                canvas.writeString(graphStartX, logY,     "SYS: Frame Blit zero-copy: OK", 0x64748B, 0x0E1726);
                canvas.writeString(graphStartX, logY + 1, "JNI: Win32 Console Thread: OK", 0x64748B, 0x0E1726);
                canvas.writeString(graphStartX, logY + 2, "MSG: Pipeline active at 120Hz", 0x22C55E, 0x0E1726);

                // 9. WINDOW BOTTOM STATUS STRIP
                for (int c = startX + 1; c < startX + winW - 1; c++) {
                    canvas.writeCell(c, startY + winH - 2, ' ', 0xFFFFFF, 0x0F172A);
                }
                canvas.writeString(startX + 2, startY + winH - 2, "● LIVE LINK", 0x10B981, 0x0F172A);
                canvas.writeString(startX + winW - 15, startY + winH - 2, "SECURE CONNECTION", 0x64748B, 0x0F172A);
            }

            // Blit standard composite buffers to screen
            canvas.setDirty(true);
            renderer.render();

            // 120 FPS sleep throttle
            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = frameTimeMs - elapsed;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private static int getNeonColor(double huePhase) {
        int r = (int) (Math.sin(huePhase) * 120 + 135);
        int g = (int) (Math.sin(huePhase + 2.0 * Math.PI / 3.0) * 120 + 135);
        int b = (int) (Math.sin(huePhase + 4.0 * Math.PI / 3.0) * 120 + 135);
        return (r << 16) | (g << 8) | b;
    }

    private static void drawProgressBar(FastTerminalScene canvas, int x, int y, int percentage, int width, int activeColor, int bgColor) {
        int filled = (percentage * width) / 100;
        for (int i = 0; i < width; i++) {
            if (i < filled) {
                canvas.writeCell(x + i, y, '█', activeColor, bgColor);
            } else {
                canvas.writeCell(x + i, y, '░', 0x334155, bgColor);
            }
        }
    }
}
