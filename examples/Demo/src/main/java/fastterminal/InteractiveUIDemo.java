package fastterminal;

import fastmouse.FastMouse;
import fastmouse.FastMouseListener;
import fastterminal.ui.Panel;
import fastterminal.ui.Button;
import fastterminal.ui.Dropdown;
import fastterminal.ui.ProgressBar;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Premium Component Presentation Showcase for FastTerminal.
 * Organizes each visual component one-by-one into a modular presentation deck.
 * Features hybrid auto-calibration for pixel-to-cell mapping under Windows Terminal.
 */
public class InteractiveUIDemo {

    private static volatile int mouseCellX = -1;
    private static volatile int mouseCellY = -1;
    private static volatile boolean mouseClicked = false;
    private static volatile boolean mousePressedState = false;

    // Calibration states for ConPTY (Windows Terminal) environments
    private static volatile boolean calibrated = false;
    private static volatile int calibClientX = 0;
    private static volatile int calibClientY = 0;
    private static volatile int calibFontW = 9;  // standard Windows Terminal default width
    private static volatile int calibFontH = 19; // standard Windows Terminal default height
    private static volatile int lastAbsoluteX = -1;
    private static volatile int lastAbsoluteY = -1;

    private static volatile int currentSlide = 0; // 0 = Panel, 1 = Button, 2 = ProgressBar, 3 = Dropdown
    private static volatile int clickCount = 0;

    public static void main(String[] args) {
        System.out.println("Initializing FastTerminal Component Slideshow Presentation...");

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

        // Calculate layout coordinates
        int tabW = 20;
        int tabH = 3;
        int startX = (cols - (tabW * 4 + 6)) / 2;
        if (startX < 2) startX = 2;

        // 1. Create Top Palette Navigation Tabs (Buttons)
        List<Button> tabs = new ArrayList<>();
        
        Button tab1 = new Button(startX, 4, tabW, tabH, "1. SOLID PANEL", () -> currentSlide = 0);
        Button tab2 = new Button(startX + tabW + 2, 4, tabW, tabH, "2. CLICK BUTTON", () -> currentSlide = 1);
        Button tab3 = new Button(startX + tabW * 2 + 4, 4, tabW, tabH, "3. PROGRESS BAR", () -> currentSlide = 2);
        Button tab4 = new Button(startX + tabW * 3 + 6, 4, tabW, tabH, "4. DROPDOWN SELECT", () -> currentSlide = 3);
        
        tabs.add(tab1);
        tabs.add(tab2);
        tabs.add(tab3);
        tabs.add(tab4);

        // 2. Setup isolated Showcase Slide containers
        int showW = 60;
        int showH = 16;
        int sx = (cols - showW) / 2;
        int sy = 9;

        // Slide 0: Solid Panel
        Panel slidePanel = new Panel(sx, sy, showW, showH, 0x0F172A); // Oceanic Navy
        slidePanel.setHasShadow(true);
        slidePanel.setBorderStyle(Panel.BorderStyle.ROUNDED);
        slidePanel.setTitle("SLIDE 1: SYSTEM MONITOR");
        slidePanel.setBorderFg(0x3B82F6); // Bright blue border

        // Slide 1: Interactive Button
        Panel slideButton = new Panel(sx, sy, showW, showH, 0x1E293B); // Slate Gray
        slideButton.setHasShadow(true);
        slideButton.setBorderStyle(Panel.BorderStyle.DOUBLE);
        slideButton.setTitle("SLIDE 2: CONTROLS");
        slideButton.setBorderFg(0x10B981); // Emerald green border
        Button actionBtn = new Button(18, 6, 24, 3, "CLICK ME", () -> clickCount++);
        slideButton.add(actionBtn);

        // Slide 2: Progress Bar
        Panel slideProgress = new Panel(sx, sy, showW, showH, 0x18181B); // Dark Charcoal
        slideProgress.setHasShadow(true);
        slideProgress.setBorderStyle(Panel.BorderStyle.SINGLE);
        slideProgress.setTitle("SLIDE 3: TELEMETRY");
        slideProgress.setBorderFg(0xEAB308); // Brilliant gold border
        ProgressBar bar = new ProgressBar(10, 8, 40, 1);
        slideProgress.add(bar);

        // Slide 3: Dropdown Selector
        Panel slideDropdown = new Panel(sx, sy, showW, showH, 0x1E1B4B); // Deep Indigo
        slideDropdown.setHasShadow(true);
        slideDropdown.setBorderStyle(Panel.BorderStyle.ROUNDED);
        slideDropdown.setTitle("SLIDE 4: THEME SELECT");
        slideDropdown.setBorderFg(0x818CF8); // Indigo border
        Dropdown combo = new Dropdown(15, 6, 30, Arrays.asList("Theme: Emerald Green", "Theme: Indigo Blue", "Theme: Crimson Red"), (index) -> {
            if (index == 0) {
                bar.setFillBg(0x10B981); // Emerald
                actionBtn.setHoverBg(0xEAB308); // Gold
            } else if (index == 1) {
                bar.setFillBg(0x6366F1); // Indigo
                actionBtn.setHoverBg(0x10B981); // Emerald
            } else if (index == 2) {
                bar.setFillBg(0xEF4444); // Crimson
                actionBtn.setHoverBg(0x6366F1); // Indigo
            }
        });
        slideDropdown.add(combo);
        slideDropdown.add(combo);

        // Try Win32 Auto-Calibration first
        try {
            int[] winInfo = FastTerminal.getConsoleWindowInfo();
            if (winInfo != null && winInfo[4] > 0 && winInfo[5] > 0 && winInfo[2] > 0 && winInfo[3] > 0) {
                calibClientX = winInfo[2];
                calibClientY = winInfo[3];
                calibFontW = winInfo[4];
                calibFontH = winInfo[5];
                calibrated = true;
            }
        } catch (Throwable ignored) {}

        // Initialize FastMouse background listener
        FastMouse mouse = FastMouse.open();
        mouse.startListening(new FastMouseListener() {
            @Override
            public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY) {
                lastAbsoluteX = absoluteX;
                lastAbsoluteY = absoluteY;

                if (calibrated) {
                    int relX = absoluteX - calibClientX;
                    int relY = absoluteY - calibClientY;
                    mouseCellX = relX / calibFontW;
                    mouseCellY = relY / calibFontH;
                }
            }

            @Override
            public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                if (buttonId == 0) {
                    mousePressedState = isPressed;
                    if (isPressed) {
                        mouseClicked = true;
                    }
                }
            }

            @Override
            public void onMouseWheel(long deviceHandle, int delta) {
                clickCount += (delta > 0 ? 1 : -1);
                if (clickCount < 0) clickCount = 0;
            }
        });

        long frameTimeMs = 1000 / 120; // 120 FPS target

        while (true) {
            long startTime = System.currentTimeMillis();

            // Resize management
            int[] size = FastTerminal.getWindowSize(cols, rows);
            if (renderer.resize(size[0], size[1])) {
                cols = size[0];
                rows = size[1];
                canvas.resize(cols, rows);

                // Recenter Top Navigation Tabs
                int newStartX = (cols - (tabW * 4 + 6)) / 2;
                if (newStartX < 2) newStartX = 2;

                tab1.setX(newStartX);
                tab2.setX(newStartX + tabW + 2);
                tab3.setX(newStartX + tabW * 2 + 4);
                tab4.setX(newStartX + tabW * 3 + 6);

                // Recenter Showcase Area
                int newSx = (cols - showW) / 2;
                int newSy = 9;

                int dx = newSx - slidePanel.getX();
                int dy = newSy - slidePanel.getY();

                slidePanel.setX(newSx); slidePanel.setY(newSy);

                slideButton.setX(newSx); slideButton.setY(newSy);
                actionBtn.setX(actionBtn.getX() + dx); actionBtn.setY(actionBtn.getY() + dy);

                slideProgress.setX(newSx); slideProgress.setY(newSy);
                bar.setX(bar.getX() + dx); bar.setY(bar.getY() + dy);

                slideDropdown.setX(newSx); slideDropdown.setY(newSy);
                combo.setX(combo.getX() + dx); combo.setY(combo.getY() + dy);
            }

            canvas.clear();

            // Render background
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    canvas.writeCell(c, r, ' ', 0x475569, 0x090D16);
                }
            }

            // Sync mouse coordinates
            int mx = mouseCellX;
            int my = mouseCellY;

            if (!calibrated) {
                // RENDER CALIBRATION TARGET TARGET SCREEN
                int calibW = 46;
                int calibH = 5;
                int cx = (cols - calibW) / 2;
                int cy = rows / 2 - 2;

                // Draw calibration button card
                for (int r = 0; r < calibH; r++) {
                    for (int c = 0; c < calibW; c++) {
                        canvas.writeCell(cx + c, cy + r, ' ', 0xFFFFFF, 0x1E293B);
                    }
                }
                // Draw card borders
                for (int c = 0; c < calibW; c++) {
                    canvas.writeCell(cx + c, cy, '─', 0x3B82F6, 0x1E293B);
                    canvas.writeCell(cx + c, cy + calibH - 1, '─', 0x3B82F6, 0x1E293B);
                }
                for (int r = 0; r < calibH; r++) {
                    canvas.writeCell(cx, cy + r, '│', 0x3B82F6, 0x1E293B);
                    canvas.writeCell(cx + calibW - 1, cy + r, '│', 0x3B82F6, 0x1E293B);
                }
                canvas.writeCell(cx, cy, '┌', 0x3B82F6, 0x1E293B);
                canvas.writeCell(cx + calibW - 1, cy, '┐', 0x3B82F6, 0x1E293B);
                canvas.writeCell(cx, cy + calibH - 1, '└', 0x3B82F6, 0x1E293B);
                canvas.writeCell(cx + calibW - 1, cy + calibH - 1, '┘', 0x3B82F6, 0x1E293B);

                canvas.writeString(cx + 4, cy + 2, "🎯 CLICK HERE TO CALIBRATE MOUSE 🎯", 0xFBBF24, 0x1E293B);

                int bannerX = (cols - 50) / 2;
                canvas.writeString(bannerX, 2, "⚡ FASTTERMINAL MOUSE CALIBRATION REQUIRED ⚡", 0xEAB308, 0x090D16);
                canvas.writeString(bannerX - 5, 4, "Windows Terminal ConPTY detected: pixel offsets must be aligned.", 0x94A3B8, 0x090D16);
                canvas.writeString(bannerX + 2, rows - 5, "Please click in the golden target box above.", 0x64748B, 0x090D16);

                if (mouseClicked) {
                    // One-click calibration offset calculation based on click target center
                    int targetCenterX = cx + (calibW / 2);
                    int targetCenterY = cy + (calibH / 2);

                    calibClientX = lastAbsoluteX - (targetCenterX * calibFontW);
                    calibClientY = lastAbsoluteY - (targetCenterY * calibFontH);
                    calibrated = true;
                    mouseClicked = false;
                }
            } else {
                // RENDER NORMAL PRESENATION DECK

                // Update Top Palette navigation tabs
                for (int i = 0; i < tabs.size(); i++) {
                    Button tab = tabs.get(i);
                    // Highlight active presentation tab
                    if (currentSlide == i) {
                        tab.setNormalBg(0xEAB308); // Gold backdrop for active selection
                        tab.setHoverBg(0xFDE047);  // Bright yellow when hovering active selection
                    } else {
                        tab.setNormalBg(0x27272A); // Zinc gray for unselected
                        tab.setHoverBg(0xEAB308);  // Gold when hovering unselected selection
                    }
                    tab.handleMouseMove(mx, my);
                }

                // Route mouse updates to active showcase container
                Panel activeShowcase = null;
                if (currentSlide == 0) activeShowcase = slidePanel;
                else if (currentSlide == 1) activeShowcase = slideButton;
                else if (currentSlide == 2) activeShowcase = slideProgress;
                else if (currentSlide == 3) activeShowcase = slideDropdown;

                if (activeShowcase != null) {
                    activeShowcase.handleMouseMove(mx, my);
                }

                // Dispatch mouse clicks
                if (mouseClicked) {
                    boolean tabHandled = false;
                    for (Button tab : tabs) {
                        if (tab.handleMouseClick(mx, my, true)) {
                            tabHandled = true;
                            break;
                        }
                    }
                    if (!tabHandled && activeShowcase != null) {
                        activeShowcase.handleMouseClick(mx, my, true);
                    }
                    mouseClicked = false;
                } else if (!mousePressedState) {
                    for (Button tab : tabs) {
                        tab.handleMouseClick(mx, my, false);
                    }
                    if (activeShowcase != null) {
                        activeShowcase.handleMouseClick(mx, my, false);
                    }
                }

                // Progress bar syncing
                double currentProgress = (clickCount % 11) / 10.0;
                bar.setProgress(currentProgress);

                // 1. Render Top Tabs Palette
                for (Button tab : tabs) {
                    tab.render(canvas);
                }

                // 2. Render Active Showcase Slide
                if (activeShowcase != null) {
                    activeShowcase.render(canvas);
                }

                // Render custom text labels and guides inside active slides
                if (currentSlide == 0) {
                    int py0 = slidePanel.getY();
                    int px0 = slidePanel.getX();
                    canvas.writeString(px0 + 4, py0 + 2, "⚡ 1. SOLID BORDERLESS PANEL ⚡", 0xEAB308, slidePanel.getBgColor());
                    canvas.writeString(px0 + 4, py0 + 5, "This window pane has a solid background color", 0x94A3B8, slidePanel.getBgColor());
                    canvas.writeString(px0 + 4, py0 + 6, "with zero border drawing overhead. It features", 0x94A3B8, slidePanel.getBgColor());
                    canvas.writeString(px0 + 4, py0 + 7, "a clean, balancing Lanterna-style drop shadow", 0x94A3B8, slidePanel.getBgColor());
                    canvas.writeString(px0 + 4, py0 + 8, "offset by 2 columns right and 1 row bottom.", 0x94A3B8, slidePanel.getBgColor());
                } else if (currentSlide == 1) {
                    int py1 = slideButton.getY();
                    int px1 = slideButton.getX();
                    canvas.writeString(px1 + 4, py1 + 2, "⚡ 2. INTERACTIVE BUTTON ⚡", 0xEAB308, slideButton.getBgColor());
                    canvas.writeString(px1 + 4, py1 + 11, String.format("Button Counter: %d clicks", clickCount), 0x38BDF8, slideButton.getBgColor());
                    canvas.writeString(px1 + 4, py1 + 13, "Hover and click the button to see visual states!", 0x64748B, slideButton.getBgColor());
                } else if (currentSlide == 2) {
                    int py2 = slideProgress.getY();
                    int px2 = slideProgress.getX();
                    canvas.writeString(px2 + 4, py2 + 2, "⚡ 3. DYNAMIC PROGRESS BAR ⚡", 0xEAB308, slideProgress.getBgColor());
                    canvas.writeString(px2 + 4, py2 + 11, String.format("Current Progress: %.0f%%", currentProgress * 100), 0x34D399, slideProgress.getBgColor());
                    canvas.writeString(px2 + 4, py2 + 13, "[Scroll mouse wheel or click Tab 2 to increase!]", 0x64748B, slideProgress.getBgColor());
                } else if (currentSlide == 3) {
                    int py3 = slideDropdown.getY();
                    int px3 = slideDropdown.getX();
                    canvas.writeString(px3 + 4, py3 + 2, "⚡ 4. LAYERED DROPDOWN SELECT ⚡", 0xEAB308, slideDropdown.getBgColor());
                    canvas.writeString(px3 + 4, py3 + 12, "Expands downwards overlaying components", 0x94A3B8, slideDropdown.getBgColor());
                    canvas.writeString(px3 + 4, py3 + 13, "and fires event selection listeners instantly.", 0x94A3B8, slideDropdown.getBgColor());
                }

                // Render glowing main dashboard header
                int bannerX = (cols - 50) / 2;
                canvas.writeString(bannerX, 1, "⚡ FASTTERMINAL COMPONENT PALETTE PRESENTATION ⚡", 0xEAB308, 0x090D16);

                // Bottom Presentation Telemetry
                String status = String.format("Mouse Cell: (%d, %d) | Slide: %d/4 | Telemetry Counter: %d", mx, my, currentSlide + 1, clickCount);
                int statusX = (cols - status.length()) / 2;
                canvas.writeString(statusX, rows - 3, status, 0xE2E8F0, 0x090D16);
            }

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
