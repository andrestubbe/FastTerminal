package fastterminal;

import fastansi.FastANSI;
import fastterminal.FastTerminal;
import fastterminal.FastTerminalRenderer;
import fastterminal.demoscene.DemosceneEffect;
import fastterminal.demoscene.effects.IntroEffect;
import fastterminal.demoscene.effects.GradientEffect;
import fastterminal.demoscene.effects.ImageZoomEffect;
import fastterminal.demoscene.effects.DoomFireEffect;
import fastterminal.demoscene.effects.CheckerboardEffect;
import fastterminal.demoscene.effects.CubeEffect;
import fastterminal.demoscene.effects.AttractorEffect;

import fastkeyboard.FastKeyboard;
import fastkeyboard.FastKeyboardImpl;

/**
 * @class Megademo
 * @brief High-performance modular Demoscene Megademo suite.
 * 
 * Auto-cycles through active visual effects every 60 seconds.
 * Integrates direct low-level key listeners for Left/Right arrow key navigation.
 */
public class Demo {

    private static volatile int activeEffectIndex = 0;
    private static volatile long lastSwitchTime = System.currentTimeMillis();
    private static volatile boolean effectChanged = false;

    /**
     * @brief Main program entry point.
     * 
     * Configures alternative screen buffer, enables hardware keyboard query hook,
     * registers and runs active demoscene visual effects in a synchronized 120 FPS render loop.
     * 
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        System.out.println("Initializing FastTerminal Demoscene Suite...");

        // Initialize FastKeyboard JNI listener
        final FastKeyboard keyboard = new FastKeyboardImpl();

        // Shutdown hook to cleanly exit alternative buffer
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print(FastANSI.ALT_BUFFER_OFF + FastANSI.CURSOR_SHOW + FastANSI.RESET);
            try {
                keyboard.stopListening();
            } catch (Throwable ignored) {}
        }));

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

        // Register effects in cyclical array
        final DemosceneEffect[] effects = {
            new IntroEffect(),
            new GradientEffect(),
            new ImageZoomEffect(),
            new DoomFireEffect(),
            new CheckerboardEffect(),
            new CubeEffect(),
            new AttractorEffect()
        };

        // Initialize all effects
        for (DemosceneEffect effect : effects) {
            effect.init(cols, rows);
        }

        // Start listening to hardware arrow keys under active terminal focus gate
        keyboard.startListening((deviceHandle, vKey, makeCode, isPressed, isE0, timestamp, keyChar) -> {
            if (isPressed) {
                // Focus gate check: keystrokes only apply if terminal is active window!
                if (!FastTerminal.isTerminalFocused()) {
                    return;
                }

                if (vKey == 0x25) { // VK_LEFT (Left arrow)
                    activeEffectIndex = (activeEffectIndex - 1 + effects.length) % effects.length;
                    lastSwitchTime = System.currentTimeMillis();
                    effectChanged = true;
                } else if (vKey == 0x27) { // VK_RIGHT (Right arrow)
                    activeEffectIndex = (activeEffectIndex + 1) % effects.length;
                    lastSwitchTime = System.currentTimeMillis();
                    effectChanged = true;
                }
            }
        });

        long frameCounter = 0;
        long frameTimeTargetMs = 1000 / 120; // 120 FPS target
        long lastFpsUpdateTime = System.currentTimeMillis();
        int fpsFrameCount = 0;
        double realFps = 120.0;

        while (true) {
            long startTime = System.nanoTime();

            // 1. Handle viewport resizing
            int[] size = FastTerminal.getWindowSize(cols, rows);
            if (renderer.resize(size[0], size[1])) {
                cols = size[0];
                rows = size[1];
                canvas.resize(cols, rows);
                
                // Re-initialize all effects to support new aspect bounds!
                for (DemosceneEffect effect : effects) {
                    effect.init(cols, rows);
                }
            }

            // 2. Select current effect and check 60s auto-cycle or keystroke skip
            long now = System.currentTimeMillis();
            if (now - lastSwitchTime >= 60_000 || effectChanged) {
                if (!effectChanged) {
                    activeEffectIndex = (activeEffectIndex + 1) % effects.length;
                    lastSwitchTime = now;
                }
                effectChanged = false;
                canvas.clear();
                renderer.clearPrev();
            }

            DemosceneEffect activeEffect = effects[activeEffectIndex];

            // 3. Update physics and render current scene
            activeEffect.update(frameCounter++);
            activeEffect.render(canvas);

            // 4. Overlap high-fidelity translucent status overlays in the dead vertical center
            double timeRemaining = 60.0 - (now - lastSwitchTime) / 1000.0;
            if (timeRemaining < 0) timeRemaining = 0;

            String line1 = " [ FASTTERMINAL TRUE-COLOR ] ";
            String line2 = String.format("  %s  ", stripEmojis(activeEffect.getName()));
            String line3 = String.format(" %.1f FPS - %.1fs ", realFps, timeRemaining);

            int centerY = rows / 2 - 1;

            // Write glowing overlays with dark transparent contrast backings
            canvas.writeString((cols - line1.length()) / 2, centerY - 1, line1, 0x000000, 0xF59E0B); // Black on Solid Yellow
            canvas.writeString((cols - line2.length()) / 2, centerY,     line2, 0xFFFFFF, 0x07070F); // Plain White, no brackets!
            canvas.writeString((cols - line3.length()) / 2, centerY + 1, line3, 0xF59E0B, 0x07070F); // Neon Yellow Stats

            // 5. Render buffer to standard output
            renderer.render();

            fpsFrameCount++;
            long timeNow = System.currentTimeMillis();
            if (timeNow - lastFpsUpdateTime >= 1000) {
                realFps = (fpsFrameCount * 1000.0) / (timeNow - lastFpsUpdateTime);
                fpsFrameCount = 0;
                lastFpsUpdateTime = timeNow;
            }

            long elapsedNs = System.nanoTime() - startTime;
            long elapsedMs = elapsedNs / 1_000_000;

            if (elapsedMs < frameTimeTargetMs) {
                try {
                    Thread.sleep(frameTimeTargetMs - elapsedMs);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    /**
     * @brief Normalizes input text to strip supplementary symbols and emoji characters.
     * 
     * @param text Original source string.
     * @return Cleaned string containing printable ASCII only.
     */
    private static String stripEmojis(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            int cp = text.codePointAt(i);
            // Keep only standard printable ASCII range
            if (cp >= 32 && cp <= 126) {
                sb.appendCodePoint(cp);
            }
            if (Character.isSupplementaryCodePoint(cp)) {
                i++; // Skip low surrogate
            }
        }
        return sb.toString().trim();
    }
}
