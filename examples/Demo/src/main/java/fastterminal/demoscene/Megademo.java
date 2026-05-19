package fastterminal.demoscene;

import fastterminal.Ansi;
import fastterminal.FastTerminal;
import fastterminal.FastTerminalRenderer;
import fastterminal.FastTerminalScene;
import fastterminal.demoscene.effects.DoomFireEffect;
import fastterminal.demoscene.effects.PlasmaEffect;
import fastterminal.demoscene.effects.MatrixRainEffect;
import fastterminal.demoscene.effects.WarpStarfieldEffect;

/**
 * High-performance modular Demoscene Megademo suite.
 * Auto-cycles through active effects every 60 seconds.
 */
public class Megademo {

    public static void main(String[] args) {
        System.out.println("Initializing FastTerminal Megademo Suite...");

        // Shutdown hook to cleanly exit alternative buffer
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Ansi.print(Ansi.EXIT_ALT_BUFFER, Ansi.SHOW_CURSOR, Ansi.RESET);
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
        DemosceneEffect[] effects = {
            new DoomFireEffect(),
            new PlasmaEffect(),
            new MatrixRainEffect(),
            new WarpStarfieldEffect()
        };

        // Initialize all effects
        for (DemosceneEffect effect : effects) {
            effect.init(cols, rows);
        }

        int activeEffectIndex = 0;
        long lastSwitchTime = System.currentTimeMillis();
        long frameCounter = 0;

        long frameTimeTargetMs = 1000 / 60; // 60 FPS target
        long lastTime = System.nanoTime();

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

            // 2. Select current effect and check 60s auto-cycle timer
            long now = System.currentTimeMillis();
            if (now - lastSwitchTime >= 60_000) {
                activeEffectIndex = (activeEffectIndex + 1) % effects.length;
                lastSwitchTime = now;
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
            String footer = String.format(" Active Effect: %s | Auto-cycling in: %.1f seconds | Framerate: 60 FPS ", 
                    activeEffect.getName(), timeRemaining);

            // Write glowing overlays with dark transparent contrast backings
            canvas.writeString((cols - header.length()) / 2, 1, header, 0xF59E0B, 0x07070F);
            canvas.writeString((cols - footer.length()) / 2, rows - 2, footer, 0x10B981, 0x07070F);

            // 5. Render buffer to standard output
            renderer.render();

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
