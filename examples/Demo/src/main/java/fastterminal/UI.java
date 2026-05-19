package fastterminal;

import fastmouse.FastMouse;
import fastmouse.FastMouseListener;
import fastterminal.Ansi;
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
        Ansi.print(Ansi.ENTER_ALT_BUFFER, Ansi.HIDE_CURSOR);

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
                int relX = absX - clientX;
                int relY = absY - clientY;
                
                double w = (clientWidth > 0) ? (double) clientWidth / currentCols : fontW;
                double h = (clientHeight > 0) ? (double) clientHeight / currentRows : fontH;
                
                mouseCellX = (int) (relX / w);
                mouseCellY = (int) (relY / h);
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
            Ansi.print(Ansi.EXIT_ALT_BUFFER, Ansi.SHOW_CURSOR, Ansi.RESET);
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

            // 1. Draw dynamic background spatial grid
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    int bg = 0x0A1931; // Beautiful premium dark blue background
                    if (x % 10 == 0 || y % 5 == 0) {
                        bg = 0x15305B; // Tech grid lines in lighter blue
                    }
                    canvas.writeCell(x, y, ' ', -1, bg);
                }
            }

            // 2. Draw glowing neon coordinate crosshairs aligned to mouse coordinates
            int mx = mouseCellX;
            int my = mouseCellY;

            if (mx >= 0 && mx < cols && my >= 0 && my < rows) {
                int crosshairColor = isLeftPressed ? 0xEF4444 : isRightPressed ? 0x3B82F6 : 0x10B981; // Red on Left, Blue on Right, Neon Green on Hover

                // Horizontal line
                for (int x = 0; x < cols; x++) {
                    int dist = Math.abs(x - mx);
                    double intensity = 1.0 - (double) dist / cols;
                    int finalColor = Gradient.interpolate(crosshairColor, 0x0A1931, 1.0 - intensity);
                    
                    if (x != mx) {
                        canvas.writeCell(x, my, '┄', finalColor, 0x0F2D54);
                    }
                }

                // Vertical line
                for (int y = 0; y < rows; y++) {
                    int dist = Math.abs(y - my);
                    double intensity = 1.0 - (double) dist / rows;
                    int finalColor = Gradient.interpolate(crosshairColor, 0x0A1931, 1.0 - intensity);
                    
                    if (y != my) {
                        canvas.writeCell(mx, y, '┆', finalColor, 0x0F2D54);
                    }
                }

                // Draw central hotspot node (white mouse on dark blue background)
                int hotspotBg = isLeftPressed ? 0xEF4444 : isRightPressed ? 0x3B82F6 : 0x0A1931;
                canvas.writeCell(mx, my, '✦', 0xFFFFFF, hotspotBg);
            }

            // 3. Render telemetry dashboard overlay panel
            int panelW = 44;
            int panelH = 10;
            int panelX = 4;
            int panelY = 2;

            // Render panel backing and glowing frame
            for (int y = 0; y < panelH; y++) {
                for (int x = 0; x < panelW; x++) {
                    int px = panelX + x;
                    int py = panelY + y;
                    if (px >= 0 && px < cols && py >= 0 && py < rows) {
                        char ch = ' ';
                        int borderCol = 0x475569; // Slate gray frame
                        if (x == 0 && y == 0) ch = '┏';
                        else if (x == panelW - 1 && y == 0) ch = '┓';
                        else if (x == 0 && y == panelH - 1) ch = '┗';
                        else if (x == panelW - 1 && y == panelH - 1) ch = '┛';
                        else if (x == 0 || x == panelW - 1) ch = '┃';
                        else if (y == 0 || y == panelH - 1) ch = '━';

                        int bg = (ch == ' ') ? 0x0A0F1D : 0x1E293B;
                        canvas.writeCell(px, py, ch, borderCol, bg);
                    }
                }
            }

            // Draw status text telemetry inside panel
            canvas.writeString(panelX + 3, panelY + 1, " 🪐 NATIVE MOUSE TELEMETRY ", 0xF59E0B, 0x0B0F19);
            canvas.writeString(panelX + 3, panelY + 3, String.format("Cell Coordinates: (%d, %d)", mx, my), 0xE2E8F0, 0x0A0F1D);
            canvas.writeString(panelX + 3, panelY + 4, String.format("Absolute Pixels: (%d, %d)", absoluteX, absoluteY), 0xE2E8F0, 0x0A0F1D);
            canvas.writeString(panelX + 3, panelY + 5, String.format("Left Button     : %s", isLeftPressed ? "🔴 DOWN" : "⚪ UP"), isLeftPressed ? 0xEF4444 : 0x94A3B8, 0x0A0F1D);
            canvas.writeString(panelX + 3, panelY + 6, String.format("Right Button    : %s", isRightPressed ? "🔵 DOWN" : "⚪ UP"), isRightPressed ? 0x3B82F6 : 0x94A3B8, 0x0A0F1D);
            canvas.writeString(panelX + 3, panelY + 7, String.format("Total Clicked   : %d times", clickCount), 0x10B981, 0x0A0F1D);
            canvas.writeString(panelX + 3, panelY + 8, String.format("Focused State   : %b", FastTerminal.isTerminalFocused()), 0x38BDF8, 0x0A0F1D);

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
