package fastterminal;

import fastmouse.FastMouse;
import fastmouse.FastMouseListener;
import fastmouse.MouseDevice;

import java.util.List;

/**
 * Dedicated Hardware Mouse Telemetry & Calibration Visualizer.
 * Provides interactive terminal mouse diagnostics, displaying:
 * - Connected hardware mouse devices
 * - Raw hardware screen coordinates vs. Client client area offsets
 * - Calculated character-cell coordinates
 * - Live button states (Left, Right, Middle, buttons 4/5) and wheel scrolls
 * - Live interactive drawing canvas to test tracking accuracy.
 */
public class MouseTelemetryDemo {

    private static volatile long lastDeviceHandle = -1;
    private static volatile int rawX = -1;
    private static volatile int rawY = -1;
    private static volatile int cellX = -1;
    private static volatile int cellY = -1;
    
    private static volatile boolean leftPressed = false;
    private static volatile boolean rightPressed = false;
    private static volatile boolean middlePressed = false;
    private static volatile int scrollTicks = 0;

    // Drawing Canvas
    private static final int CANVAS_W = 50;
    private static final int CANVAS_H = 15;
    private static final char[][] canvasData = new char[CANVAS_H][CANVAS_W];

    static {
        for (int r = 0; r < CANVAS_H; r++) {
            for (int c = 0; c < CANVAS_W; c++) {
                canvasData[r][c] = '.';
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting FastMouse Telemetry Diagnostic Utility...");

        // Register shutdown hook to clean up console on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Ansi.print(Ansi.EXIT_ALT_BUFFER, Ansi.SHOW_CURSOR, Ansi.RESET);
        }));

        // Enter alternate screen buffer and hide standard cursor
        Ansi.print(Ansi.ENTER_ALT_BUFFER, Ansi.HIDE_CURSOR);

        int cols = 100;
        int rows = 35;

        // Try to query starting size natively
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

        // Open FastMouse interface
        FastMouse mouse = FastMouse.open();
        List<MouseDevice> devices = mouse.getConnectedDevices();

        // Start listening
        mouse.startListening(new FastMouseListener() {
            @Override
            public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY) {
                lastDeviceHandle = deviceHandle;
                rawX = absoluteX;
                rawY = absoluteY;

                // Calibrate mouse to cell coordinates
                int[] winInfo = FastTerminal.getConsoleWindowInfo();
                if (winInfo != null) {
                    int clientX = winInfo[2];
                    int clientY = winInfo[3];
                    int fontW = winInfo[4];
                    int fontH = winInfo[5];

                    int relX = absoluteX - clientX;
                    int relY = absoluteY - clientY;

                    if (fontW > 0 && fontH > 0) {
                        cellX = relX / fontW;
                        cellY = relY / fontH;

                        // Draw trail if left button is pressed
                        int canvasStartX = (cols() - CANVAS_W) / 2;
                        int canvasStartY = 16;
                        int cx = cellX - canvasStartX;
                        int cy = cellY - canvasStartY;
                        if (cx >= 0 && cx < CANVAS_W && cy >= 0 && cy < CANVAS_H) {
                            if (leftPressed) {
                                canvasData[cy][cx] = '█';
                            } else if (rightPressed) {
                                canvasData[cy][cx] = '.';
                            }
                        }
                    }
                }
            }

            @Override
            public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                lastDeviceHandle = deviceHandle;
                if (buttonId == 0) {
                    leftPressed = isPressed;
                } else if (buttonId == 1) {
                    rightPressed = isPressed;
                } else if (buttonId == 2) {
                    middlePressed = isPressed;
                }
            }

            @Override
            public void onMouseWheel(long deviceHandle, int delta) {
                lastDeviceHandle = deviceHandle;
                scrollTicks += (delta > 0 ? 1 : -1);
            }

            private int cols() {
                int[] size = FastTerminal.getWindowSize(100, 35);
                return size[0];
            }
        });

        long frameTimeMs = 1000 / 60; // 60 FPS update loop

        while (true) {
            long startTime = System.currentTimeMillis();

            // Resize management
            int[] size = FastTerminal.getWindowSize(cols, rows);
            if (renderer.resize(size[0], size[1])) {
                cols = size[0];
                rows = size[1];
                canvas.resize(cols, rows);
            }

            canvas.clear();

            // Render deep backdrop
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    canvas.writeCell(c, r, ' ', 0x475569, 0x090D16);
                }
            }

            // Draw header banner
            int bannerX = (cols - 50) / 2;
            canvas.writeString(bannerX, 1, "⚡ FASTMOUSE DEDICATED HARDWARE TELEMETRY ⚡", 0x38BDF8, 0x090D16);
            canvas.writeString(bannerX + 5, 2, "Isolating raw input streams and calibration", 0x64748B, 0x090D16);

            // 1. Draw Connected Mouse Hardware list
            int leftPanelX = 4;
            canvas.writeString(leftPanelX, 5, "🔌 Connected Mouse Hardware:", 0xE2E8F0, 0x090D16);
            int py = 7;
            if (devices.isEmpty()) {
                canvas.writeString(leftPanelX + 2, py, "No registered mouse devices detected via Raw Input.", 0xEF4444, 0x090D16);
            } else {
                for (MouseDevice dev : devices) {
                    String devStr = String.format("• ID: %08X | Buttons: %d | %s", dev.getHandle(), dev.getButtonCount(), dev.getName());
                    canvas.writeString(leftPanelX + 2, py++, devStr.substring(0, Math.min(devStr.length(), cols - 10)), 0x10B981, 0x090D16);
                }
            }

            // 2. Draw Live Telemetry Diagnostics
            int teleX = 4;
            int teleY = py + 2;
            canvas.writeString(teleX, teleY, "📊 Live Mouse Telemetry Indicators:", 0xE2E8F0, 0x090D16);
            
            // Console metrics
            int[] winInfo = FastTerminal.getConsoleWindowInfo();
            String winInfoStr = "Unknown (Failed to query console metrics)";
            if (winInfo != null) {
                winInfoStr = String.format("ClientOffset=(%d, %d) | FontCell=(%d x %d)", winInfo[2], winInfo[3], winInfo[4], winInfo[5]);
            }
            
            canvas.writeString(teleX + 2, teleY + 2, String.format("Last Active Device Handle : 0x%08X", lastDeviceHandle), 0xFBBF24, 0x090D16);
            canvas.writeString(teleX + 2, teleY + 3, String.format("Raw Screen Coordinates    : (%d, %d)", rawX, rawY), 0x60A5FA, 0x090D16);
            canvas.writeString(teleX + 2, teleY + 4, String.format("Console Window Metrics    : %s", winInfoStr), 0xA78BFA, 0x090D16);
            canvas.writeString(teleX + 2, teleY + 5, String.format("Calculated Character Cell : (%d, %d)", cellX, cellY), 0x34D399, 0x090D16);

            // Active button indicators
            canvas.writeString(teleX + 2, teleY + 7, "Button States:", 0xE2E8F0, 0x090D16);
            canvas.writeString(teleX + 18, teleY + 7, " LEFT ", 0xFFFFFF, leftPressed ? 0x10B981 : 0x27272A);
            canvas.writeString(teleX + 26, teleY + 7, " MIDDLE ", 0xFFFFFF, middlePressed ? 0x3B82F6 : 0x27272A);
            canvas.writeString(teleX + 36, teleY + 7, " RIGHT ", 0xFFFFFF, rightPressed ? 0xEF4444 : 0x27272A);
            canvas.writeString(teleX + 46, teleY + 7, String.format("Scroll Ticks: %d", scrollTicks), 0xF472B6, 0x090D16);

            // 3. Interactive Drawing Canvas Grid
            int canvasStartX = (cols - CANVAS_W) / 2;
            int canvasStartY = 16;
            
            canvas.writeString(canvasStartX, canvasStartY - 2, "🎨 Interactive Drawing Calibration Pad:", 0xE2E8F0, 0x090D16);
            canvas.writeString(canvasStartX, canvasStartY - 1, "[Hold LEFT mouse button to DRAW | Hold RIGHT mouse button to ERASE]", 0x64748B, 0x090D16);

            // Draw canvas borders
            for (int c = 0; c < CANVAS_W + 2; c++) {
                canvas.writeCell(canvasStartX - 1 + c, canvasStartY - 1, '─', 0x475569, 0x090D16);
                canvas.writeCell(canvasStartX - 1 + c, canvasStartY + CANVAS_H, '─', 0x475569, 0x090D16);
            }
            for (int r = 0; r < CANVAS_H; r++) {
                canvas.writeCell(canvasStartX - 1, canvasStartY + r, '│', 0x475569, 0x090D16);
                canvas.writeCell(canvasStartX + CANVAS_W, canvasStartY + r, '│', 0x475569, 0x090D16);
            }
            canvas.writeCell(canvasStartX - 1, canvasStartY - 1, '┌', 0x475569, 0x090D16);
            canvas.writeCell(canvasStartX + CANVAS_W, canvasStartY - 1, '┐', 0x475569, 0x090D16);
            canvas.writeCell(canvasStartX - 1, canvasStartY + CANVAS_H, '└', 0x475569, 0x090D16);
            canvas.writeCell(canvasStartX + CANVAS_W, canvasStartY + CANVAS_H, '┘', 0x475569, 0x090D16);

            // Draw canvas cells
            for (int r = 0; r < CANVAS_H; r++) {
                for (int c = 0; c < CANVAS_W; c++) {
                    char ch = canvasData[r][c];
                    int fg = (ch == '█' ? 0x10B981 : 0x334155);
                    canvas.writeCell(canvasStartX + c, canvasStartY + r, ch, fg, 0x090D16);
                }
            }

            // Draw temporary live cursor pointer on top of the canvas
            int cx = cellX - canvasStartX;
            int cy = cellY - canvasStartY;
            if (cx >= 0 && cx < CANVAS_W && cy >= 0 && cy < CANVAS_H) {
                canvas.writeCell(cellX, cellY, '✦', 0xEAB308, 0x090D16);
            }

            // Bottom exit instructions
            String exitGuide = "Press Ctrl+C to close and exit the diagnostic utility.";
            canvas.writeString((cols - exitGuide.length()) / 2, rows - 2, exitGuide, 0x64748B, 0x090D16);

            renderer.render();

            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed < frameTimeMs) {
                try {
                    Thread.sleep(frameTimeMs - elapsed);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
