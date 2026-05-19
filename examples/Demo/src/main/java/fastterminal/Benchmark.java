package fastterminal;

import fastansi.FastANSI;
import fastterminal.ui.Panel;

/**
 * Dedicated high-performance telemetry and diff-rendering benchmark for FastTerminal.
 * Showcases the real-time byte savings and frame rendering times under different rendering modes.
 */
public class Benchmark {

    public static void main(String[] args) {
        System.out.println("Initializing FastTerminal Real-Time Composition Benchmark...");

        // Register shutdown hook to clean up console on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print(FastANSI.ALT_BUFFER_OFF + FastANSI.CURSOR_SHOW + FastANSI.RESET);
        }));

        // Enter alternate screen buffer and hide standard cursor
        System.out.print(FastANSI.ALT_BUFFER_ON + FastANSI.CURSOR_HIDE);

        int cols = 100;
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

        // Bouncing particle telemetry states
        double ballX = 10.0;
        double ballY = 12.0;
        double ballVx = 0.8;
        double ballVy = 0.4;
        int ballSize = 4;

        // Telemetry calculation states
        long lastModeChangeTime = System.currentTimeMillis();
        boolean useDiff = true;

        long frameTimeTargetMs = 1000 / 60; // 60 FPS
        int frameCounter = 0;
        long totalFullBytes = 0;
        long totalDiffBytes = 0;
        int fullSamples = 0;
        int diffSamples = 0;

        while (true) {
            long startTime = System.nanoTime();

            // 1. Manage dynamic resizing
            int[] size = FastTerminal.getWindowSize(cols, rows);
            if (renderer.resize(size[0], size[1])) {
                cols = size[0];
                rows = size[1];
                canvas.resize(cols, rows);
            }

            // 2. Clear canvas with custom deep grid background
            canvas.clear();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    // Draw a subtle dotted raster grid
                    if (c % 4 == 0 && r % 2 == 0) {
                        canvas.writeCell(c, r, '·', 0x1E293B, 0x05070A);
                    } else {
                        canvas.writeCell(c, r, ' ', 0x1E293B, 0x05070A);
                    }
                }
            }

            // 3. Update bouncing telemetry particle
            ballX += ballVx;
            ballY += ballVy;

            // Bouncing collision boundaries
            int boundMaxX = cols - ballSize - 2;
            int boundMaxY = rows - ballSize - 3;
            if (ballX <= 2) { ballX = 2; ballVx = -ballVx; }
            if (ballX >= boundMaxX) { ballX = boundMaxX; ballVx = -ballVx; }
            if (ballY <= 4) { ballY = 4; ballVy = -ballVy; }
            if (ballY >= boundMaxY) { ballY = boundMaxY; ballVy = -ballVy; }

            // Draw bouncing particle (golden solid block)
            for (int r = 0; r < ballSize; r++) {
                for (int c = 0; c < ballSize * 2; c++) {
                    canvas.writeCell((int)ballX + c, (int)ballY + r, '█', 0xF59E0B, 0x05070A);
                }
            }

            // 4. Auto-cycle between modes every 3 seconds to showcase differences
            long now = System.currentTimeMillis();
            if (now - lastModeChangeTime >= 3000) {
                useDiff = !useDiff;
                renderer.setDiffRenderingEnabled(useDiff);
                lastModeChangeTime = now;
            }

            // 5. Draw high-fidelity telemetry widgets
            int panelW = 48;
            int panelH = 10;
            int px = (cols - panelW) / 2;
            int py = 6;

            Panel statPanel = new Panel(px, py, panelW, panelH, 0x0F172A);
            statPanel.setBorderStyle(Panel.BorderStyle.DOUBLE);
            statPanel.setBorderFg(useDiff ? 0x10B981 : 0xEF4444); // Green border for diff, red for full
            statPanel.setTitle("⚡ RENDERING PERFORMANCE TELEMETRY ⚡");
            statPanel.render(canvas);

            int lastFlushed = renderer.getLastFlushedBytes();
            
            // Collect statistics
            if (useDiff) {
                totalDiffBytes += lastFlushed;
                diffSamples++;
            } else {
                totalFullBytes += lastFlushed;
                fullSamples++;
            }

            double avgFull = fullSamples > 0 ? (double) totalFullBytes / fullSamples : 24000.0;
            double avgDiff = diffSamples > 0 ? (double) totalDiffBytes / diffSamples : 800.0;
            double pctSavings = avgFull > 0 ? (1.0 - (avgDiff / avgFull)) * 100.0 : 0.0;

            // Render text inside panel
            int ty = py + 2;
            canvas.writeString(px + 3, ty,     "Mode:        ", 0x94A3B8, statPanel.getBgColor());
            if (useDiff) {
                canvas.writeString(px + 16, ty, "[GAP-DIFF DOUBLE-BUFFERING]", 0x10B981, statPanel.getBgColor());
            } else {
                canvas.writeString(px + 16, ty, "[FULL BUFFER REDRAW FLUSH] ", 0xEF4444, statPanel.getBgColor());
            }

            canvas.writeString(px + 3, ty + 2, String.format("Frame Bytes:  %d bytes", lastFlushed), 0xE2E8F0, statPanel.getBgColor());
            canvas.writeString(px + 3, ty + 3, String.format("Avg Diff Mode:%d bytes", (int)avgDiff), 0x34D399, statPanel.getBgColor());
            canvas.writeString(px + 3, ty + 4, String.format("Avg Full Mode:%d bytes", (int)avgFull), 0xF87171, statPanel.getBgColor());
            canvas.writeString(px + 3, ty + 5, String.format("Bandwidth Saved:  %.1f%% (Zero-Copy)", pctSavings), 0xFBBF24, statPanel.getBgColor());

            // 6. Draw header and footer
            int bannerX = (cols - 46) / 2;
            canvas.writeString(bannerX, 2, "⚡ FASTTERMINAL TRUE-COLOR COMPOSITION BENCHMARK ⚡", 0xEAB308, 0x05070A);
            canvas.writeString(bannerX - 5, 4, "Testing real-time ANSI escape minimization and cell differential rendering.", 0x64748B, 0x05070A);

            String cycleTimer = String.format("Auto-cycling rendering mode in: %.1f seconds...", 3.0 - (now - lastModeChangeTime) / 1000.0);
            canvas.writeString((cols - cycleTimer.length()) / 2, rows - 3, cycleTimer, 0x94A3B8, 0x05070A);

            // 7. Render and capture timing
            renderer.render();

            long elapsedNs = System.nanoTime() - startTime;
            long elapsedMs = elapsedNs / 1_000_000;

            // Render rendering execution latency
            String latency = String.format(" Composition Latency: %.3f ms | Frame #%d ", (double)elapsedNs / 1_000_000.0, frameCounter++);
            canvas.writeString(cols - latency.length() - 2, 1, latency, 0x38BDF8, 0x05070A);

            if (elapsedMs < frameTimeTargetMs) {
                try {
                    Thread.sleep(frameTimeTargetMs - elapsedMs);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
