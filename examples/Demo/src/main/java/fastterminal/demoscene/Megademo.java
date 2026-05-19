package fastterminal.demoscene;

import fastterminal.Ansi;
import fastterminal.FastTerminal;
import fastterminal.FastTerminalRenderer;
import fastterminal.FastTerminalScene;
import fastterminal.demoscene.effects.DoomFireEffect;
import fastterminal.demoscene.effects.PlasmaEffect;
import fastterminal.demoscene.effects.MatrixRainEffect;
import fastterminal.demoscene.effects.WarpStarfieldEffect;
import fastterminal.demoscene.effects.CheckerboardEffect;
import fastterminal.demoscene.effects.MetaballsEffect;
import fastterminal.demoscene.effects.TunnelEffect;
import fastterminal.demoscene.effects.LissajousEffect;
import fastterminal.demoscene.effects.PlanetEffect;
import fastterminal.demoscene.effects.MandelbrotEffect;
import fastterminal.demoscene.effects.WavefallEffect;
import fastterminal.demoscene.effects.CubeEffect;
import fastterminal.demoscene.effects.ShadedEffect;
import fastterminal.demoscene.effects.GradientEffect;
import fastterminal.demoscene.effects.StarNestEffect;
import fastterminal.demoscene.effects.TwisterEffect;
import fastterminal.demoscene.effects.JuliaEffect;
import fastterminal.demoscene.effects.BouncingBallsEffect;
import fastterminal.demoscene.effects.FluidGridEffect;
import fastterminal.demoscene.effects.ColorCycleEffect;
import fastterminal.demoscene.effects.AsciiTunnelEffect;
import fastterminal.demoscene.effects.AttractorEffect;
import fastterminal.demoscene.effects.TorusEffect;
import fastterminal.demoscene.effects.VolcanoEffect;
import fastterminal.demoscene.effects.RaycasterEffect;
import fastterminal.demoscene.effects.GalagaEffect;
import fastterminal.demoscene.effects.TerrainEffect;
import fastterminal.demoscene.effects.FluidDynamicsEffect;
import fastterminal.demoscene.effects.LifeEffect;
import fastterminal.demoscene.effects.Spaceship3DEffect;
import fastterminal.demoscene.effects.AudioVisualizerEffect;
import fastterminal.demoscene.effects.FluidSandboxEffect;
import fastterminal.demoscene.effects.MatrixTunnelEffect;
import fastterminal.demoscene.effects.PlasmaGlobeEffect;
import fastterminal.demoscene.effects.WarpTunnelEffect;
import fastkeyboard.FastKeyboard;
import fastkeyboard.FastKeyboardImpl;

/**
 * High-performance modular Demoscene Megademo suite.
 * Auto-cycles through active effects every 60 seconds.
 * Supports Left/Right arrow keys for navigation when focused.
 */
public class Megademo {

    private static volatile int activeEffectIndex = 0;
    private static volatile long lastSwitchTime = System.currentTimeMillis();
    private static volatile boolean effectChanged = false;

    public static void main(String[] args) {
        System.out.println("Initializing FastTerminal Megademo Suite...");

        // Initialize FastKeyboard JNI listener
        final FastKeyboard keyboard = new FastKeyboardImpl();

        // Shutdown hook to cleanly exit alternative buffer
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Ansi.print(Ansi.EXIT_ALT_BUFFER, Ansi.SHOW_CURSOR, Ansi.RESET);
            try {
                keyboard.stopListening();
            } catch (Throwable ignored) {}
        }));

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

        FastTerminalRenderer renderer = new FastTerminalRenderer(cols, rows);
        FastTerminalScene canvas = new FastTerminalScene(0, 0, cols, rows);
        renderer.addScene(canvas);

        // Register effects in cyclical array
        final DemosceneEffect[] effects = {
            new DoomFireEffect(),
            new PlasmaEffect(),
            new MatrixRainEffect(),
            new WarpStarfieldEffect(),
            new CheckerboardEffect(),
            new MetaballsEffect(),
            new TunnelEffect(),
            new LissajousEffect(),
            new PlanetEffect(),
            new MandelbrotEffect(),
            new WavefallEffect(),
            new CubeEffect(),
            new ShadedEffect(),
            new GradientEffect(),
            new StarNestEffect(),
            new TwisterEffect(),
            new JuliaEffect(),
            new BouncingBallsEffect(),
            new FluidGridEffect(),
            new ColorCycleEffect(),
            new AsciiTunnelEffect(),
            new AttractorEffect(),
            new TorusEffect(),
            new VolcanoEffect(),
            new RaycasterEffect(),
            new GalagaEffect(),
            new TerrainEffect(),
            new FluidDynamicsEffect(),
            new LifeEffect(),
            new Spaceship3DEffect(),
            new AudioVisualizerEffect(),
            new FluidSandboxEffect(),
            new MatrixTunnelEffect(),
            new PlasmaGlobeEffect(),
            new WarpTunnelEffect()
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

            // 4. Overlap high-fidelity translucent status overlays
            double timeRemaining = 60.0 - (now - lastSwitchTime) / 1000.0;
            String header = " ⚡ FASTTERMINAL MEGADEMO SUITE v0.1.0 ⚡ ";
            String footer = String.format(" Active Effect: %s | Auto-cycling in: %.1f seconds | Framerate: %.1f FPS | Controls: ◄ / ► Keys ", 
                    activeEffect.getName(), timeRemaining, realFps);

            // Write glowing overlays with dark transparent contrast backings
            canvas.writeString((cols - header.length()) / 2, 1, header, 0xF59E0B, 0x07070F);
            canvas.writeString((cols - footer.length()) / 2, rows - 2, footer, 0x10B981, 0x07070F);

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
}
