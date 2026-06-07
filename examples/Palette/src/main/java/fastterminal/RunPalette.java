package fastterminal;

import fastansi.FastANSI;
import fastkeyboard.FastKeyboard;
import fastkeyboard.FastKeyboardImpl;
import fastkeyboard.FastKeyboardListener;
import fastmouse.FastMouseListener;
import fastterminal.component.*;
import fastterminal.composable.*;
import fastterminal.layout.*;

import java.util.Arrays;
import java.util.Random;

public class RunPalette {
    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 30;

    private static volatile int mouseCellX = -1;
    private static volatile int mouseCellY = -1;
    private static volatile boolean isLeftPressed = false;
    
    private static TextBox focusedTextBox = null;

    public static void main(String[] args) throws Exception {
        System.out.println("Initializing Palette Demo...");

        boolean useSwing = false;
        for (String arg : args) {
            if (arg.equals("--swing")) useSwing = true;
        }

        int cols = DEFAULT_WIDTH;
        int rows = DEFAULT_HEIGHT;
        try {
            int[] size = FastTerminal.getTerminalSize();
            if (size != null && size[0] > 0 && size[1] > 0) {
                cols = size[0];
                rows = size[1];
            }
        } catch (Throwable ignored) {}

        if (!useSwing) {
            System.out.print(FastANSI.ALT_BUFFER_ON + FastANSI.CURSOR_HIDE);
        }
        
        FastTerminalRenderer renderer = null;
        fastterminal.swing.SwingTerminalRenderer swingRenderer = null;
        FastTerminalScene canvas = new FastTerminalScene(0, 0, cols, rows);
        
        if (useSwing) {
            swingRenderer = new fastterminal.swing.SwingTerminalRenderer(cols, rows, "FastTerminal Component Palette");
        } else {
            renderer = new FastTerminalRenderer(cols, rows);
            renderer.addScene(canvas);
        }

        int dashW = Math.max(60, cols - 10);
        int dashH = Math.max(20, rows - 4);
        int dashX = (cols - dashW) / 2;
        int dashY = (rows - dashH) / 2;

        Panel dashboard = new Panel(dashX, dashY, dashW, dashH, 0x111111);
        dashboard.setBorderStyle(Panel.BorderStyle.ROUNDED);
        dashboard.setBorderFg(0x555555);
        dashboard.setHasHeaderBar(true);
        dashboard.setHeaderBg(0x333333);
        dashboard.setHeaderFg(0xFFFFFF);
        dashboard.setTitle("FastTerminal Component Palette");

        Tabs tabs = new Tabs(0, 1, dashW, dashH - 1);
        
        // --- TAB 1: Linear Layout ---
        Panel pnlLinear = new Panel(0, 0, dashW, dashH - 3, 0x111111);
        Panel l1 = new Panel(0, 0, dashW - 4, 4, 0x551111); l1.setTitle("Item 1"); l1.setHasHeaderBar(true); l1.setBorderStyle(Panel.BorderStyle.SINGLE); l1.setBorderFg(0x773333);
        Panel l2 = new Panel(0, 0, dashW - 4, 4, 0x115511); l2.setTitle("Item 2"); l2.setHasHeaderBar(true); l2.setBorderStyle(Panel.BorderStyle.SINGLE); l2.setBorderFg(0x337733);
        Panel l3 = new Panel(0, 0, dashW - 4, 4, 0x111155); l3.setTitle("Item 3"); l3.setHasHeaderBar(true); l3.setBorderStyle(Panel.BorderStyle.SINGLE); l3.setBorderFg(0x333377);
        
        pnlLinear.add(l1);
        pnlLinear.add(l2);
        pnlLinear.add(l3);
        
        LinearLayout linear = new LinearLayout(LinearLayout.Direction.VERTICAL, 1);
        linear.layout(2, 2, dashW - 4, dashH - 6, pnlLinear.getChildren());

        // --- TAB 2: Grid Layout ---
        Panel pnlGrid = new Panel(0, 0, dashW, dashH - 3, 0x111111);
        Panel g1 = new Panel(0, 0, 0, 0, 0x551111); g1.setTitle("Row 0, Col 0"); g1.setHasHeaderBar(true); g1.setBorderStyle(Panel.BorderStyle.SINGLE); g1.setBorderFg(0x773333);
        Panel g2 = new Panel(0, 0, 0, 0, 0x115511); g2.setTitle("Row 0, Col 1"); g2.setHasHeaderBar(true); g2.setBorderStyle(Panel.BorderStyle.SINGLE); g2.setBorderFg(0x337733);
        Panel g3 = new Panel(0, 0, 0, 0, 0x111155); g3.setTitle("Row 1, Col 0"); g3.setHasHeaderBar(true); g3.setBorderStyle(Panel.BorderStyle.SINGLE); g3.setBorderFg(0x333377);
        Panel g4 = new Panel(0, 0, 0, 0, 0x555511); g4.setTitle("Row 1, Col 1"); g4.setHasHeaderBar(true); g4.setBorderStyle(Panel.BorderStyle.SINGLE); g4.setBorderFg(0x777733);
        
        pnlGrid.add(g1);
        pnlGrid.add(g2);
        pnlGrid.add(g3);
        pnlGrid.add(g4);

        GridLayout grid = new GridLayout(2, 2, 2, 1);
        grid.layout(2, 2, dashW - 4, dashH - 6, pnlGrid.getChildren());

        // --- TAB 3: Border Layout ---
        Panel pnlBorder = new Panel(0, 0, dashW, dashH - 3, 0x111111);
        Panel n = new Panel(0,0,0,3, 0x551111); n.setTitle("North"); n.setHasHeaderBar(true); n.setBorderStyle(Panel.BorderStyle.SINGLE); n.setBorderFg(0x773333);
        Panel s = new Panel(0,0,0,3, 0x115511); s.setTitle("South"); s.setHasHeaderBar(true); s.setBorderStyle(Panel.BorderStyle.SINGLE); s.setBorderFg(0x337733);
        Panel e = new Panel(0,0,15,0, 0x111155); e.setTitle("East"); e.setHasHeaderBar(true); e.setBorderStyle(Panel.BorderStyle.SINGLE); e.setBorderFg(0x333377);
        Panel w = new Panel(0,0,15,0, 0x555511); w.setTitle("West"); w.setHasHeaderBar(true); w.setBorderStyle(Panel.BorderStyle.SINGLE); w.setBorderFg(0x777733);
        Panel centerPanel = new Panel(0,0,0,0, 0x222222); centerPanel.setTitle("Center"); centerPanel.setHasHeaderBar(true); centerPanel.setBorderStyle(Panel.BorderStyle.SINGLE); centerPanel.setBorderFg(0x555555);
        
        pnlBorder.add(n);
        pnlBorder.add(s);
        pnlBorder.add(e);
        pnlBorder.add(w);
        pnlBorder.add(centerPanel);
        
        BorderLayout border = new BorderLayout();
        border.setNorth(n);
        border.setSouth(s);
        border.setEast(e);
        border.setWest(w);
        border.setCenter(centerPanel);
        border.layout(2, 2, dashW - 4, dashH - 6, pnlBorder.getChildren());
        
        tabs.addTab("Linear Layout", pnlLinear);
        tabs.addTab("Grid Layout", pnlGrid);
        tabs.addTab("Border Layout", pnlBorder);
        
        dashboard.add(tabs);

        final boolean finalUseSwing = useSwing;
        FastKeyboardListener kbListener = (deviceHandle, vKey, makeCode, isPressed, isE0, timestamp, keyCharStr) -> {
            if (isPressed && (finalUseSwing || FastTerminal.isTerminalFocused())) {
                if (vKey == 0x1B) System.exit(0); // ESC
                if (focusedTextBox != null) {
                    char c = (keyCharStr != null && !keyCharStr.isEmpty()) ? keyCharStr.charAt(0) : '\0';
                    focusedTextBox.handleKey(vKey, c);
                }
            }
        };

        FastMouseListener msListener = new FastMouseListener() {
            @Override
            public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absX, int absY) {
                mouseCellX = absX;
                mouseCellY = absY;
                if (isLeftPressed) {
                    dashboard.handleMouseDrag(absX, absY);
                } else {
                    dashboard.handleMouseMove(absX, absY);
                }
            }

            @Override
            public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                if (buttonId == 0) {
                    isLeftPressed = isPressed;
                    if (isPressed) {
                        if (dashboard.isCloseClick(mouseCellX, mouseCellY)) System.exit(0);
                    }
                    if (!dashboard.handleMouseClick(mouseCellX, mouseCellY, isPressed)) {
                        if (focusedTextBox != null) focusedTextBox.setFocused(false);
                        focusedTextBox = null;
                    }
                }
            }

            @Override
            public void onMouseWheel(long deviceHandle, int delta) {
                // Not used in bare layout demo
            }
        };

        FastKeyboard keyboard = null;
        AnsiMouse mouse = null;
        
        if (useSwing) {
            swingRenderer.setKeyboardListener(kbListener);
            swingRenderer.setMouseListener(msListener);
        } else {
            keyboard = new FastKeyboardImpl();
            keyboard.startListening(kbListener);
            mouse = AnsiMouse.open(msListener);
        }

        final FastKeyboard finalKeyboard = keyboard;
        final AnsiMouse finalMouse = mouse;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!finalUseSwing) {
                System.out.print(FastANSI.ALT_BUFFER_OFF + FastANSI.CURSOR_SHOW + FastANSI.RESET);
                try { if (finalKeyboard != null) finalKeyboard.stopListening(); } catch (Throwable ignored) {}
                try { if (finalMouse != null) finalMouse.close(); } catch (Throwable ignored) {}
            }
        }));

        Random random = new Random();
        int percent = 0;
        int[] history = new int[20];

        while (true) {
            long startTime = System.currentTimeMillis();

            canvas.clear();
            dashboard.render(canvas);
            
            if (mouseCellX >= 0 && mouseCellY >= 0) {
                int cursorBg = isLeftPressed ? 0xEF4444 : 0x10B981;
                canvas.writeCellAlpha(mouseCellX, mouseCellY, '↖', 0xFFFFFF, cursorBg, 1.0, 0.4);
            }

            if (useSwing) {
                swingRenderer.render(canvas);
            } else {
                renderer.render();
            }

            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = 16 - elapsed; // ~60fps
            if (sleepTime > 0) Thread.sleep(sleepTime);
        }
    }
}
