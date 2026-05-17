package fastterminal;

import org.jline.terminal.Terminal;

public class CLIScene {

    private final Terminal terminal;
    private TerminalRenderer renderer;
    private TerminalScene sceneTitle;
    private TerminalScene sceneLogger;
    private LogListener logListener;

    public CLIScene(final Terminal terminal) {
        this.terminal = terminal;
        this.addScene();
        this.setupResizeListener();
    }

    private void addScene() {
        this.renderer = new TerminalRenderer(this.terminal.getWidth(), this.terminal.getHeight());
        
        // Setup simple Title Scene
        this.sceneTitle = new TerminalScene(0, 0, this.renderer.getWidth(), 1);
        this.sceneTitle.setUpdater(() -> {
            int[] b = this.sceneTitle.getCodepointBuffer();
            String title = " FastTerminal Engine | Press 'q' to quit | 'o' to open editor";
            for (int i = 0; i < Math.min(title.length(), this.sceneTitle.getWidth()); i++) {
                b[i] = title.charAt(i);
            }
        });
        
        // Setup simple Logger Scene
        this.sceneLogger = new TerminalScene(0, 1, this.renderer.getWidth(), this.renderer.getHeight() - 1);
        this.sceneLogger.setUpdater(() -> {
            final java.util.List<String> list = Log.getLastLines(this.sceneLogger.getHeight());
            int[] b = this.sceneLogger.getCodepointBuffer();
            for (int i = 0; i < list.size(); i++) {
                final String logLine = list.get(i);
                final int bufferStart = i * this.sceneLogger.getWidth();
                // FIX: Clean the line range to avoid trailing characters from older long lines
                java.util.Arrays.fill(b, bufferStart, bufferStart + this.sceneLogger.getWidth(), ' ');
                for (int j = 0; j < Math.min(logLine.length(), this.sceneLogger.getWidth()); j++) {
                    b[bufferStart + j] = logLine.charAt(j);
                }
            }
        });
        
        this.renderer.addScene(this.sceneTitle);
        this.renderer.addScene(this.sceneLogger);
        
        this.logListener = line -> {
            this.sceneTitle.setDirty(true);
            this.sceneLogger.setDirty(true);
            this.renderer.render();
        };
        Log.addListener(this.logListener);
    }

    private void removeScene() {
        Log.removeListener(this.logListener);
        this.sceneTitle.dispose();
        this.sceneLogger.dispose();
        this.renderer.dispose();
    }

    private void setupResizeListener() {
        final Terminal.SignalHandler signalHandler = signal -> {
            this.removeScene();
            this.addScene();
            this.renderer.render();
        };
        this.terminal.handle(Terminal.Signal.WINCH, signalHandler);
    }
}
