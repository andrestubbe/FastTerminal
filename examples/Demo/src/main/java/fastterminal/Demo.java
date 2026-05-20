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
import fastterminal.demoscene.effects.MatrixZoomEffect;

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
    private static volatile int prevEffectIndex   = 0;
    private static volatile long lastSwitchTime = System.currentTimeMillis();
    private static volatile boolean effectChanged = false;
    private static volatile long cycleDurationMs = 60_000;

    /** Duration of the crossfade between scenes, in seconds. */
    private static final double FADE_DURATION_S = 5.0;
    /** 0.0 = showing only prevEffect, 1.0 = fully on activeEffect. */
    private static volatile double fadeProgress = 1.0;

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
            new AttractorEffect(),
            new MatrixZoomEffect()
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
                } else if (vKey == 0x26) { // VK_UP (Up arrow) - Increase switch cycle by 5 seconds
                    cycleDurationMs += 5000;
                } else if (vKey == 0x28) { // VK_DOWN (Down arrow) - Decrease switch cycle by 5 seconds (min 5s)
                    cycleDurationMs = Math.max(5000, cycleDurationMs - 5000);
                }
            }
        });

        long frameTimeTargetMs = 1000 / 60; // 60 FPS target for extreme stability and low CPU usage
        long lastFpsUpdateTime = System.currentTimeMillis();
        int fpsFrameCount = 0;
        double realFps = 60.0;

        long suiteStartTime = System.currentTimeMillis();
        double prevTime = 0.0;

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

            // 2. Select current effect and check auto-cycle or keystroke skip
            long now = System.currentTimeMillis();
            if (now - lastSwitchTime >= cycleDurationMs || effectChanged) {
                if (!effectChanged) {
                    prevEffectIndex   = activeEffectIndex;
                    activeEffectIndex = (activeEffectIndex + 1) % effects.length;
                    lastSwitchTime    = now;
                } else {
                    prevEffectIndex = activeEffectIndex; // arrow-key jump: fade from current
                }
                effectChanged = false;
                fadeProgress  = 0.0; // kick off a new crossfade
                canvas.clear();
                renderer.clearPrev();
            }

            double time = (now - suiteStartTime) / 1000.0;
            double deltaTime = time - prevTime;
            if (deltaTime < 0.0) deltaTime = 0.0;
            if (deltaTime > 0.1) deltaTime = 0.1;
            prevTime = time;

            DemosceneEffect activeEffect = effects[activeEffectIndex];
            DemosceneEffect prevEffect   = effects[prevEffectIndex];

            // Advance fade (0 → 1 over FADE_DURATION_S seconds)
            if (fadeProgress < 1.0) {
                fadeProgress = Math.min(1.0, fadeProgress + deltaTime / FADE_DURATION_S);
            }

            // 3. Render and apply fade-through-black transition
            if (fadeProgress >= 1.0 || prevEffectIndex == activeEffectIndex) {
                // No fade active — direct single render
                activeEffect.update(time, deltaTime);
                activeEffect.render(canvas);
            } else {
                // Fade-through-black:
                //   Phase 1 (t 0.0 → 0.5): outgoing scene dims to black
                //   Phase 2 (t 0.5 → 1.0): incoming scene brightens from black
                // Only ONE effect renders per frame. The black midpoint hides the
                // character-content switch, so it works for any pair of effects.
                double t = fadeProgress; // 0 → 1

                // Smooth ease-in / ease-out for each phase independently
                double brightness;
                DemosceneEffect toRender;
                if (t < 0.5) {
                    // Dim outgoing: brightness 1 → 0
                    double p = t * 2.0;                          // 0 → 1
                    double ease = p * p;                         // ease-in (accelerate into black)
                    brightness = 1.0 - ease;
                    toRender = prevEffect;
                } else {
                    // Brighten incoming: brightness 0 → 1
                    double p = (t - 0.5) * 2.0;                  // 0 → 1
                    double ease = 1.0 - (1.0 - p) * (1.0 - p);  // ease-out (decelerate from black)
                    brightness = ease;
                    toRender = activeEffect;
                }

                // Always keep the active effect updated so it isn't frozen on entry
                activeEffect.update(time, deltaTime);
                if (toRender == prevEffect) prevEffect.update(time, deltaTime);

                toRender.render(canvas);

                // Multiply every fg and bg color channel in-place toward black
                int[] fg = canvas.getFgBuffer();
                int[] bg = canvas.getBgBuffer();
                int n = cols * rows;
                for (int i = 0; i < n; i++) {
                    fg[i] = scaleColor(fg[i], brightness);
                    bg[i] = scaleColor(bg[i], brightness);
                }
            }

            // 4. Overlap high-fidelity translucent status overlays in the dead vertical center
            double cycleSecs = cycleDurationMs / 1000.0;
            double timeRemaining = cycleSecs - (now - lastSwitchTime) / 1000.0;
            if (timeRemaining < 0) timeRemaining = 0;

            String line1 = " [ FASTTERMINAL TRUE-COLOR ] ";
            String line2 = String.format("  %s  ", stripEmojis(activeEffect.getName()));
            String line3 = String.format(" %.1f FPS - %.1fs / %.0fs ", realFps, timeRemaining, cycleSecs);

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

    /**
     * @brief Linearly interpolates two packed 24-bit RGB colors channel by channel.
     *
     * @param a Start color (packed 0xRRGGBB).
     * @param b End color   (packed 0xRRGGBB).
     * @param t Blend factor in [0, 1]: 0 = fully a, 1 = fully b.
     * @return Blended packed RGB color.
     */
    private static int lerpColor(int a, int b, double t) {
        int rA = (a >> 16) & 0xFF, gA = (a >> 8) & 0xFF, bA = a & 0xFF;
        int rB = (b >> 16) & 0xFF, gB = (b >> 8) & 0xFF, bB = b & 0xFF;
        int r = (int) (rA + (rB - rA) * t);
        int g = (int) (gA + (gB - gA) * t);
        int bl = (int) (bA + (bB - bA) * t);
        return (r << 16) | (g << 8) | bl;
    }

    /**
     * @brief Scales a packed 24-bit RGB color by a brightness multiplier.
     *
     * @param color   Packed 0xRRGGBB color (-1 treated as black).
     * @param brightness 0.0 = black, 1.0 = original color.
     * @return Scaled packed RGB color.
     */
    private static int scaleColor(int color, double brightness) {
        if (color < 0) return 0;
        int r = (int) (((color >> 16) & 0xFF) * brightness);
        int g = (int) (((color >>  8) & 0xFF) * brightness);
        int b = (int) (( color        & 0xFF) * brightness);
        return (r << 16) | (g << 8) | b;
    }
}
