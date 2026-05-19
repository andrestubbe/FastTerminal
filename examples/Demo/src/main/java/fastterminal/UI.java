package fastterminal;

import fastansi.FastANSI;
import fastmouse.FastMouse;
import fastmouse.FastMouseListener;
import fastterminal.FastTerminal;
import fastterminal.FastTerminalRenderer;
import fastterminal.FastTerminalScene;
import fastterminal.Gradient;

/**
 * 🌌 Real-Time Native Mouse Interactive Visualizer.
 * Displays real-time cell coordinate tracking, absolute screen pixel offsets,
 * active button click indicators, and a beautiful glowing neon crosshair.
 */
public class UI {

    private static volatile int mouseCellX = -1;
    private static volatile int mouseCellY = -1;
    private static volatile int absoluteX = 0;
    private static volatile int absoluteY = 0;
    private static volatile boolean isLeftPressed = false;
    private static volatile boolean isRightPressed = false;
    private static volatile int clickCount = 0;

    // Volatile real-time window metrics
    private static volatile int clientX = 0;
    private static volatile int clientY = 0;
    private static volatile int clientWidth = 0;
    private static volatile int clientHeight = 0;
    private static volatile int fontW = 8;
    private static volatile int fontH = 16;
    private static volatile int currentCols = 100;
    private static volatile int currentRows = 30;

    public static void main(String[] args) {
        System.out.println("Initializing UI Mouse Visualizer...");

        // 1. Get console font and client window coordinates
        int[] winInfo = null;
        try {
            winInfo = FastTerminal.getConsoleWindowInfo();
            if (winInfo != null && winInfo.length >= 8) {
                clientX = winInfo[2];
                clientY = winInfo[3];
                fontW = winInfo[4];
                fontH = winInfo[5];
                clientWidth = winInfo[6];
                clientHeight = winInfo[7];
            }
        } catch (Throwable t) {
            System.err.println("[WARN] Could not retrieve Win32 console metrics: " + t.getMessage());
        }

        // 2. Configure screen alternate buffer and hide standard cursor
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
        currentCols = cols;
        currentRows = rows;

        FastTerminalRenderer renderer = new FastTerminalRenderer(cols, rows);
        FastTerminalScene canvas = new FastTerminalScene(0, 0, cols, rows);
        renderer.addScene(canvas);

        // 3. Register native mouse callbacks
        FastMouse mouse = FastMouse.open();
        mouse.startListening(new FastMouseListener() {
            @Override
            public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absX, int absY) {
                absoluteX = absX;
                absoluteY = absY;
                
                int extraHeight = clientHeight - (currentRows * fontH);
                int topPadding = Math.max(0, extraHeight - 8);

                int extraWidth = clientWidth - (currentCols * fontW);
                int leftPadding = 8;
                if (extraWidth < 16) {
                    leftPadding = Math.max(0, extraWidth / 2);
                }

                int relX = absX - (clientX + leftPadding);
                int relY = absY - (clientY + topPadding);
                
                int cellX = relX / fontW;
                int cellY = relY / fontH;
                
                // Safe boundary clipping to keep crosshair inside valid grid
                if (cellX < 0) cellX = 0;
                if (cellX >= currentCols) cellX = currentCols - 1;
                if (cellY < 0) cellY = 0;
                if (cellY >= currentRows) cellY = currentRows - 1;
                
                mouseCellX = cellX;
                mouseCellY = cellY;
            }

            @Override
            public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                if (buttonId == 0) {
                    isLeftPressed = isPressed;
                    if (isPressed) clickCount++;
                } else if (buttonId == 1) {
                    isRightPressed = isPressed;
                    if (isPressed) clickCount++;
                }
            }

            @Override
            public void onMouseWheel(long deviceHandle, int delta) {}
        });

        // Safe cleanup shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print(FastANSI.ALT_BUFFER_OFF + FastANSI.CURSOR_SHOW + FastANSI.RESET);
            try {
                mouse.stopListening();
            } catch (Throwable ignored) {}
        }));

        long frameCount = 0;

        while (true) {
            long startTime = System.currentTimeMillis();

            // Handle terminal resizing
            int[] currentSize = FastTerminal.getWindowSize(cols, rows);
            if (renderer.resize(currentSize[0], currentSize[1])) {
                cols = currentSize[0];
                rows = currentSize[1];
                currentCols = cols;
                currentRows = rows;
                canvas.resize(cols, rows);
            }

            // Update real-time console metrics from active window
            try {
                int[] currentWinInfo = FastTerminal.getConsoleWindowInfo();
                if (currentWinInfo != null && currentWinInfo.length >= 8) {
                    clientX = currentWinInfo[2];
                    clientY = currentWinInfo[3];
                    fontW = currentWinInfo[4];
                    fontH = currentWinInfo[5];
                    clientWidth = currentWinInfo[6];
                    clientHeight = currentWinInfo[7];
                }
            } catch (Throwable ignored) {}

            canvas.clear();

            // 1. Clear background to absolute pure black
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    canvas.writeCell(x, y, ' ', -1, 0x000000);
                }
            }

            // 2. Draw beautiful custom ANSI Mouse Cursor
            int mx = mouseCellX;
            int my = mouseCellY;

            if (mx >= 0 && mx < cols && my >= 0 && my < rows) {
                int cursorFg = 0xFFFFFF; // Crisp white cursor body
                int cursorBg = isLeftPressed ? 0xEF4444 : isRightPressed ? 0x3B82F6 : 0x10B981; // Glow matching click state (Red/Blue/Green)
                
                // Draw main pointer cell
                canvas.writeCell(mx, my, '⬉', cursorFg, cursorBg);
                
                // Draw glowing drop-shadow trailing cells to mimic a premium cursor shape
                if (mx + 1 < cols) {
                    canvas.writeCell(mx + 1, my, ' ', -1, isLeftPressed ? 0x991B1B : isRightPressed ? 0x1E40AF : 0x065F46);
                }
                if (my + 1 < rows) {
                    canvas.writeCell(mx, my + 1, ' ', -1, isLeftPressed ? 0x991B1B : isRightPressed ? 0x1E40AF : 0x065F46);
                }
                if (mx + 1 < cols && my + 1 < rows) {
                    canvas.writeCell(mx + 1, my + 1, ' ', -1, isLeftPressed ? 0x7F1D1D : isRightPressed ? 0x1E3A8A : 0x064E3B);
                }
            }

            // 3. Render telemetry dashboard overlay panel (flat card, no borders)
            int panelW = 44;
            int panelH = 10;
            int panelX = 4;
            int panelY = 2;

            // Render borderless panel backing using a premium dark gray slate bg
            for (int y = 0; y < panelH; y++) {
                for (int x = 0; x < panelW; x++) {
                    int px = panelX + x;
                    int py = panelY + y;
                    if (px >= 0 && px < cols && py >= 0 && py < rows) {
                        canvas.writeCell(px, py, ' ', -1, 0x0D1117); // Sleek flat dark grey background
                    }
                }
            }

            // Draw status text telemetry inside panel
            canvas.writeString(panelX + 3, panelY + 1, " 🪐 NATIVE MOUSE TELEMETRY ", 0xF59E0B, 0x161B22);
            canvas.writeString(panelX + 3, panelY + 3, String.format("Cell Coordinates: (%d, %d)", mx, my), 0xE2E8F0, 0x0D1117);
            canvas.writeString(panelX + 3, panelY + 4, String.format("Absolute Pixels: (%d, %d)", absoluteX, absoluteY), 0xE2E8F0, 0x0D1117);
            canvas.writeString(panelX + 3, panelY + 5, String.format("Left Button     : %s", isLeftPressed ? "🔴 DOWN" : "⚪ UP"), isLeftPressed ? 0xEF4444 : 0x94A3B8, 0x0D1117);
            canvas.writeString(panelX + 3, panelY + 6, String.format("Right Button    : %s", isRightPressed ? "🔵 DOWN" : "⚪ UP"), isRightPressed ? 0x3B82F6 : 0x94A3B8, 0x0D1117);
            canvas.writeString(panelX + 3, panelY + 7, String.format("Total Clicked   : %d times", clickCount), 0x10B981, 0x0D1117);
            canvas.writeString(panelX + 3, panelY + 8, String.format("Focused State   : %b", FastTerminal.isTerminalFocused()), 0x38BDF8, 0x0D1117);

            renderer.render();

            frameCount++;
            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = (1000 / 120) - elapsed; // Render at butter-smooth 120 FPS
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
