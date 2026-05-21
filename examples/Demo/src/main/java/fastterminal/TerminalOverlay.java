package fastterminal;

import fastansi.FastANSI;
import fastmouse.FastMouseListener;
import fastterminal.AnsiMouse;
import fastterminal.FastTerminal;
import fastterminal.FastTerminalRenderer;
import fastterminal.FastTerminalScene;
import fastterminal.ui.Panel;
import fastterminal.ui.FileNavigator;
import fastkeyboard.FastKeyboard;
import fastkeyboard.FastKeyboardImpl;

/**
 * @class TerminalOverlay
 * @brief Overlay demo: projects the UI panel directly on top of the existing terminal content.
 *
 * Captures the console screen buffer once at startup via ReadConsoleOutput (works in
 * conhost.exe / classic cmd; degrades to a blank background in Windows Terminal).
 * The snapshot is stored as a background scene layer so that when the panel is dragged,
 * the cells it vacates are repainted from the snapshot rather than left blank.
 *
 * Controls:
 *   Arrow keys  — navigate the file list
 *   Enter       — open folder / activate item
 *   ESC         — exit and restore terminal state
 *   Mouse       — drag, resize, minimize, close the panel
 */
public class TerminalOverlay {

    // 🎨 Panel color constants — BeOS amber theme
    public static final int PANEL_BG_COLOR      = 0xF4F4F5;
    public static final int PANEL_BORDER_COLOR  = 0xE4E4E7;
    public static final int PANEL_HEADER_BG     = 0xF0A500;
    public static final int PANEL_HEADER_FG     = 0x3D1C00;
    public static final int PANEL_SHADOW_FG     = 0x000000;
    public static final int PANEL_SHADOW_BG     = 0x000000;
    public static final double PANEL_SHADOW_ALPHA = 0.25;

    // Volatile mouse / interaction state
    private static volatile int     mouseCellX        = -1;
    private static volatile int     mouseCellY        = -1;
    private static volatile boolean isLeftPressed     = false;
    private static volatile boolean isRightPressed    = false;
    private static volatile boolean isDragging        = false;
    private static volatile boolean isResizing        = false;
    private static volatile int     dragOffsetX       = 0;
    private static volatile int     dragOffsetY       = 0;
    private static volatile boolean isMinimizePressed = false;

    private static volatile int     currentCols            = 100;
    private static volatile int     currentRows            = 30;
    private static volatile boolean lastCursorHiddenState  = false;

    public static void main(String[] args) {
        System.out.println("Initializing Terminal Overlay...");

        final FastKeyboard keyboard = new FastKeyboardImpl();

        // Query terminal size
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

        // ── Snapshot the screen buffer BEFORE hiding the cursor or writing anything ──
        // Also record the cursor row so we know exactly where to return on exit.
        FastTerminalScene bgScene = new FastTerminalScene(0, 0, cols, rows);
        final boolean[] hasSnapshot = { false };
        final int[] startCursorRow = { rows - 1 }; // fallback: last row
        try {
            int[] snap = FastTerminal.readConsoleOutput();
            if (snap != null && snap.length >= 2 && snap[0] > 0 && snap[1] > 0) {
                int snapCols = snap[0];
                int snapRows = snap[1];
                int useCols  = Math.min(snapCols, cols);
                int useRows  = Math.min(snapRows, rows);
                for (int r = 0; r < useRows; r++) {
                    for (int c = 0; c < useCols; c++) {
                        int base = 2 + (r * snapCols + c) * 3;
                        int cp   = snap[base];
                        int fg   = snap[base + 1];
                        int bg   = snap[base + 2];
                        bgScene.writeCell(c, r, cp, fg, bg);
                    }
                }
                hasSnapshot[0] = true;
            }
        } catch (Throwable ignored) {}

        if (!hasSnapshot[0]) {
            // Fallback: dark neutral background so the panel still looks good
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    bgScene.writeCell(c, r, ' ', 0xCCCCCC, 0x1E1E1E);
                }
            }
        }

        // Record cursor row AFTER snapshot but BEFORE we print anything or hide cursor.
        // This is the row "Initializing Terminal Overlay..." was printed on — the prompt
        // must return to the row immediately below it on exit.
        try {
            int[] curPos = FastTerminal.getCursorPosition();
            if (curPos != null && curPos[1] >= 0) {
                startCursorRow[0] = curPos[1]; // 0-based row
            }
        } catch (Throwable ignored) {}
        // ─────────────────────────────────────────────────────────────────────────

        // Hide cursor — no alternate buffer, stay on the main screen
        System.out.print(FastANSI.CURSOR_HIDE);

        // Renderer: bgScene (snapshot) + canvas (panel) composited in order
        FastTerminalRenderer renderer = new FastTerminalRenderer(cols, rows);
        renderer.setDiffRenderingEnabled(true);
        renderer.suppressInitialFullRedraw();

        FastTerminalScene canvas = new FastTerminalScene(0, 0, cols, rows);
        // canvas is transparent: blank cells (space, fg=-1, bg=-1) let bgScene show through.
        // This means when the panel moves, the vacated cells revert to the snapshot.
        canvas.setTransparentBackground(true);
        renderer.addScene(bgScene);   // layer 0 — snapshot background
        renderer.addScene(canvas);    // layer 1 — panel + cursor (transparent where blank)

        // Panel — centered
        int dashW = 50;
        int dashH = 15;
        int dashX = Math.max(0, (cols - dashW) / 2);
        int dashY = Math.max(0, (rows - dashH) / 2);

        Panel dashboard = new Panel(dashX, dashY, dashW, dashH, PANEL_BG_COLOR);
        dashboard.setBorderStyle(Panel.BorderStyle.ROUNDED);
        dashboard.setBorderFg(PANEL_BORDER_COLOR);
        dashboard.setHasHeaderBar(true);
        dashboard.setHeaderBg(PANEL_HEADER_BG);
        dashboard.setHeaderFg(PANEL_HEADER_FG);
        dashboard.setBeosStyle(true);
        dashboard.setTitle("File Navigator");
        dashboard.setBodyAlpha(0.95);
        dashboard.setShadowFg(PANEL_SHADOW_FG);
        dashboard.setShadowBg(PANEL_SHADOW_BG);
        dashboard.setShadowAlpha(PANEL_SHADOW_ALPHA);

        FileNavigator navigator = new FileNavigator(0, 1, dashW, dashH - 1);
        navigator.setBgColor(PANEL_BG_COLOR);
        navigator.setFgColor(0x18181B);
        navigator.setPathBarBg(0x92400E);
        navigator.setPathBarFg(0xFFFBEB);
        navigator.setSelectionBg(0xFDE68A);
        navigator.setSelectionFg(0x451A03);
        navigator.setBgAlpha(0.95);
        dashboard.add(navigator);

        // Keyboard listener
        keyboard.startListening((deviceHandle, vKey, makeCode, isPressed, isE0, timestamp, keyChar) -> {
            if (isPressed) {
                if (!FastTerminal.isTerminalFocused()) return;
                switch (vKey) {
                    case 0x1B: System.exit(0);              break; // ESC
                    case 0x26: navigator.selectPrevious();  break; // Up
                    case 0x28: navigator.selectNext();      break; // Down
                    case 0x0D: navigator.activateSelected(); break; // Enter
                }
            }
        });

        // Mouse listener
        AnsiMouse mouse = AnsiMouse.open(new FastMouseListener() {
            @Override
            public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absX, int absY) {
                int cellX = Math.max(0, Math.min(currentCols - 1, absX));
                int cellY = Math.max(0, Math.min(currentRows - 1, absY));
                mouseCellX = cellX;
                mouseCellY = cellY;

                if (isResizing) {
                    int newW = Math.max(15, cellX - dashboard.getX() + 1);
                    int newH = Math.max(5,  cellY - dashboard.getY() + 1);
                    if (dashboard.getX() + newW > currentCols) newW = currentCols - dashboard.getX();
                    if (dashboard.getY() + newH > currentRows) newH = currentRows - dashboard.getY();
                    dashboard.setWidth(newW);
                    dashboard.setHeight(newH);
                } else if (isDragging) {
                    int newX = Math.max(0, cellX - dragOffsetX);
                    int newY = Math.max(0, cellY - dragOffsetY);
                    if (newX + dashboard.getWidth()  > currentCols) newX = currentCols - dashboard.getWidth();
                    if (newY + dashboard.getHeight() > currentRows) newY = currentRows - dashboard.getHeight();
                    dashboard.setX(newX);
                    dashboard.setY(newY);
                } else {
                    dashboard.handleMouseMove(cellX, cellY);
                }
            }

            @Override
            public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                if (buttonId == 0) {
                    isLeftPressed = isPressed;
                    if (isPressed) {
                        int dx = dashboard.getX();
                        int dy = dashboard.getY();
                        int dw = dashboard.getWidth();

                        if (dashboard.isMinimized()) {
                            if (dashboard.isIconHit(mouseCellX, mouseCellY, 2, currentRows - 2)) {
                                dashboard.toggleMinimize();
                            }
                        } else if (dashboard.isResizeClick(mouseCellX, mouseCellY)) {
                            isResizing = true;
                        } else if (mouseCellX >= dx && mouseCellX < dx + dw && mouseCellY == dy) {
                            if (dashboard.isCloseClick(mouseCellX, mouseCellY)) {
                                System.exit(0);
                            } else if (dashboard.isMinimizeClick(mouseCellX, mouseCellY)) {
                                if (!dashboard.isMinimized()) dashboard.toggleMinimize();
                                isMinimizePressed = true;
                            } else {
                                isDragging  = true;
                                dragOffsetX = mouseCellX - dx;
                                dragOffsetY = mouseCellY - dy;
                            }
                        }
                    } else {
                        isDragging = false;
                        isResizing = false;
                        if (isMinimizePressed) {
                            if (dashboard.isMinimizeClick(mouseCellX, mouseCellY) && dashboard.isMinimized()) {
                                dashboard.toggleMinimize();
                            }
                            isMinimizePressed = false;
                        }
                    }
                } else if (buttonId == 1) {
                    isRightPressed = isPressed;
                }

                if (!isDragging) {
                    dashboard.handleMouseClick(mouseCellX, mouseCellY, isPressed);
                }
            }

            @Override
            public void onMouseWheel(long deviceHandle, int delta) {}
        });

        // Shutdown hook — restore terminal to its exact pre-launch state
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (hasSnapshot[0]) {
                    int[] cp  = bgScene.getCodepointBuffer();
                    int[] fg  = bgScene.getFgBuffer();
                    int[] bg  = bgScene.getBgBuffer();
                    int   w   = bgScene.getWidth();
                    int   h   = bgScene.getHeight();

                    StringBuilder sb = new StringBuilder(w * h * 20);
                    int curFg = -2, curBg = -2;

                    // Rewrite ALL rows of the snapshot — this erases the panel everywhere
                    // it was, regardless of where it was dragged to
                    for (int row = 0; row < h; row++) {
                        sb.append(FastANSI.CSI).append(row + 1).append(";1H");
                        curFg = -2; curBg = -2;
                        for (int col = 0; col < w; col++) {
                            int i   = row * w + col;
                            int c   = cp[i];
                            int f   = fg[i];
                            int b   = bg[i];
                            if (c == -99) { sb.append(' '); continue; }
                            if (f != curFg) {
                                if (f == -1) sb.append(FastANSI.FG_DEFAULT);
                                else { int r=(f>>16)&0xFF,g=(f>>8)&0xFF,bl=f&0xFF; sb.append(FastANSI.CSI).append("38;2;").append(r).append(';').append(g).append(';').append(bl).append('m'); }
                                curFg = f;
                            }
                            if (b != curBg) {
                                if (b == -1) sb.append(FastANSI.BG_DEFAULT);
                                else { int r=(b>>16)&0xFF,g=(b>>8)&0xFF,bl=b&0xFF; sb.append(FastANSI.CSI).append("48;2;").append(r).append(';').append(g).append(';').append(bl).append('m'); }
                                curBg = b;
                            }
                            if (Character.isValidCodePoint(c)) sb.appendCodePoint(c);
                            else sb.append(' ');
                        }
                    }

                    // cmd always emits a newline after the process exits before printing
                    // the next prompt — so place the cursor one row above the target,
                    // letting cmd's own newline land it exactly on the right line.
                    int promptRow = startCursorRow[0]; // 1-based: cmd adds the \n
                    if (promptRow < 1) promptRow = 1;
                    if (promptRow > h) promptRow = h;
                    sb.append(FastANSI.RESET);
                    sb.append(FastANSI.CSI).append(promptRow).append(";1H");

                    byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    System.out.write(bytes, 0, bytes.length);
                    System.out.flush();
                }
            } catch (Throwable ignored) {}
            System.out.print(FastANSI.CURSOR_SHOW + FastANSI.RESET);
            try { FastTerminal.setSystemCursorVisible(true); } catch (Throwable ignored) {}
            try { keyboard.stopListening(); }                  catch (Throwable ignored) {}
            try { mouse.close(); }                             catch (Throwable ignored) {}
        }));

        // ── Render loop ──────────────────────────────────────────────────────────
        while (true) {
            long startTime = System.currentTimeMillis();

            // Cursor visibility
            boolean shouldHide = false;
            try { shouldHide = FastTerminal.isTerminalFocused() && FastTerminal.isMouseOverTerminal(); }
            catch (Throwable ignored) {}
            if (shouldHide != lastCursorHiddenState) {
                try { FastTerminal.setSystemCursorVisible(!shouldHide); lastCursorHiddenState = shouldHide; }
                catch (Throwable ignored) {}
            }

            // Terminal resize — rebuild both scenes and re-snapshot
            int[] currentSize = FastTerminal.getWindowSize(cols, rows);
            if (renderer.resize(currentSize[0], currentSize[1])) {
                cols = currentSize[0];
                rows = currentSize[1];
                currentCols = cols;
                currentRows = rows;
                canvas.resize(cols, rows);
                bgScene.resize(cols, rows);

                // Re-center panel
                int newX = Math.max(0, (cols - dashboard.getWidth())  / 2);
                int newY = Math.max(0, (rows - dashboard.getHeight()) / 2);
                dashboard.setX(newX);
                dashboard.setY(newY);

                // Re-snapshot after resize (new visible region)
                try {
                    int[] snap = FastTerminal.readConsoleOutput();
                    if (snap != null && snap.length >= 2 && snap[0] > 0 && snap[1] > 0) {
                        int snapCols = snap[0];
                        int snapRows = snap[1];
                        int useCols  = Math.min(snapCols, cols);
                        int useRows  = Math.min(snapRows, rows);
                        for (int r = 0; r < useRows; r++) {
                            for (int c = 0; c < useCols; c++) {
                                int base = 2 + (r * snapCols + c) * 3;
                                bgScene.writeCell(c, r, snap[base], snap[base + 1], snap[base + 2]);
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }

            // canvas holds only the panel + cursor — background comes from bgScene
            canvas.clear();

            dashboard.render(canvas);
            if (dashboard.isMinimized()) {
                dashboard.renderDesktopIcon(canvas, 2, rows - 2);
            }

            // Custom cursor
            int mx = mouseCellX;
            int my = mouseCellY;
            if (mx >= 0 && mx < cols && my >= 0 && my < rows) {
                int cursorFg = 0xFFFFFF;
                int cursorBg = isLeftPressed  ? 0xEF4444
                             : isRightPressed ? 0x3B82F6
                             :                  0x10B981;
                canvas.writeCellAlpha(mx, my, '↖', cursorFg, cursorBg, 1.0, 0.4);
            }

            renderer.render();

            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = (1000 / 120) - elapsed;
            if (sleepTime > 0) {
                try { Thread.sleep(sleepTime); } catch (InterruptedException ignored) {}
            }
        }
    }
}
