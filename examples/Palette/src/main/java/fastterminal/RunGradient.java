package fastterminal;

import fastansi.FastANSI;
import fastkeyboard.FastKeyboardListener;
import fastmouse.FastMouseListener;
import fastterminal.swing.SwingTerminalRenderer;
import fastterminal.component.Component;
import fastterminal.composable.Tabs;

import fastterminal.component.Panel;
import fastterminal.util.Gradient;
import java.awt.Color;

public class RunGradient {
    private static final int DEFAULT_WIDTH = 120;
    private static final int DEFAULT_HEIGHT = 40;

    private static volatile int mouseX = 0;
    private static volatile int mouseY = 0;

    static class AnimatedGradient extends Panel {
        private String type;

        public AnimatedGradient(int x, int y, int width, int height, String type) {
            super(x, y, width, height, 0x000000);
            this.type = type;
            this.setHasShadow(false); // No shadow for full tabs
        }

        @Override
        public void render(FastTerminalScene scene) {
            long t = System.currentTimeMillis();
            
            // Cycle Hue slowly over 10 seconds
            float hue1 = (t % 10000) / 10000.0f;
            // Opposite hue for color 2
            float hue2 = (hue1 + 0.5f) % 1.0f;

            int colorStart = Color.HSBtoRGB(hue1, 1.0f, 1.0f) & 0xFFFFFF;
            int colorEnd = Color.HSBtoRGB(hue2, 1.0f, 1.0f) & 0xFFFFFF;

            switch (type) {
                case "Horizontal":
                    Gradient.applyHorizontalBg(scene, x, y, width, height, colorStart, colorEnd);
                    break;
                case "Vertical":
                    Gradient.applyVerticalBg(scene, x, y, width, height, colorStart, colorEnd);
                    break;
                case "Diagonal":
                    Gradient.applyDiagonalBg(scene, x, y, width, height, colorStart, colorEnd);
                    break;
                case "Radial":
                    Gradient.applyRadialBg(scene, x, y, width, height, colorStart, colorEnd);
                    break;
                case "Conic":
                    Gradient.applyConicBg(scene, x, y, width, height, colorStart, colorEnd);
                    break;
                case "Foreground":
                    // Apply a background gradient first (we'll reverse the colors for contrast)
                    Gradient.applyHorizontalBg(scene, x, y, width, height, colorEnd, colorStart);
                    
                    String textBlock = "FastTerminal Gradients Are Awesome! ".repeat(20);
                    for (int r = 0; r < height; r++) {
                        // Pass -2 for background to preserve the horizontal bg gradient we just drew!
                        scene.writeString(x, y + r, textBlock.substring(0, Math.min(width, textBlock.length())), -2, -2);
                    }
                    
                    // Apply the foreground diagonal gradient on top
                    Gradient.applyDiagonalFg(scene, x, y, width, height, colorStart, colorEnd);
                    break;
            }

            // Draw Label
            String label = "  " + type.toUpperCase() + " ANIMATED  ";
            int lx = x + (width / 2) - (label.length() / 2);
            int ly = y + (height / 2);
            scene.writeString(lx, ly, label, 0xFFFFFF, 0x000000);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Initializing Animated Gradient Demo...");

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
        SwingTerminalRenderer swingRenderer = null;
        FastTerminalScene canvas = new FastTerminalScene(0, 0, cols, rows);
        
        if (useSwing) {
            swingRenderer = new SwingTerminalRenderer(cols, rows, "FastTerminal Animated Gradients");
        } else {
            renderer = new FastTerminalRenderer(cols, rows);
            renderer.addScene(canvas);
        }

        Tabs tabs = new Tabs(0, 0, cols, rows);
        String[] gradientTypes = {"Horizontal", "Vertical", "Diagonal", "Radial", "Conic", "Foreground"};
        for (String type : gradientTypes) {
            tabs.addTab(type, new AnimatedGradient(0, 0, cols, rows, type));
        }

        final boolean finalUseSwing = useSwing;
        FastKeyboardListener kbListener = (deviceHandle, vKey, makeCode, isPressed, isE0, timestamp, keyCharStr) -> {
            if (isPressed && (finalUseSwing || FastTerminal.isTerminalFocused())) {
                if (vKey == 0x1B) System.exit(0); // ESC
            }
        };

        FastMouseListener msListener = new FastMouseListener() {
            @Override
            public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absX, int absY) {
                mouseX = absX;
                mouseY = absY;
                tabs.handleMouseMove(absX, absY);
            }

            @Override
            public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                if (buttonId == 0) {
                    tabs.handleMouseClick(mouseX, mouseY, isPressed);
                }
            }

            @Override
            public void onMouseWheel(long deviceHandle, int delta) {
            }
        };

        fastkeyboard.FastKeyboard keyboard = null;
        fastterminal.AnsiMouse mouse = null;

        if (useSwing) {
            swingRenderer.setKeyboardListener(kbListener);
            swingRenderer.setMouseListener(msListener);
        } else {
            keyboard = new fastkeyboard.FastKeyboardImpl();
            keyboard.startListening(kbListener);
            mouse = fastterminal.AnsiMouse.open(msListener);
        }

        final fastkeyboard.FastKeyboard finalKeyboard = keyboard;
        final fastterminal.AnsiMouse finalMouse = mouse;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!finalUseSwing) {
                System.out.print(FastANSI.ALT_BUFFER_OFF + FastANSI.CURSOR_SHOW + FastANSI.RESET);
                try { if (finalKeyboard != null) finalKeyboard.stopListening(); } catch (Throwable ignored) {}
                try { if (finalMouse != null) finalMouse.close(); } catch (Throwable ignored) {}
            }
        }));

        while (true) {
            long startTime = System.currentTimeMillis();

            float hue1 = (startTime % 10000) / 10000.0f;
            int colorStart = Color.HSBtoRGB(hue1, 1.0f, 1.0f) & 0xFFFFFF;

            tabs.setActiveTabBg(colorStart);
            tabs.setActiveTabFg(0x000000);
            tabs.setInactiveTabBg(0x000000);
            tabs.setInactiveTabFg(0x888888);

            canvas.clear();
            tabs.render(canvas);

            if (useSwing) {
                swingRenderer.render(canvas);
            } else {
                renderer.render();
            }

            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = 30 - elapsed; // ~33fps
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            } else {
                Thread.yield();
            }
        }
    }
}
