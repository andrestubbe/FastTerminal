package fastterminal;

import fastterminal.FastTerminalScene;
import fastterminal.FastTerminalRenderer;

public class Demo {
    public static void main(String[] args) throws Exception {
        FastTerminalScene scene = new FastTerminalScene(80, 24);
        FastTerminalRenderer renderer = new FastTerminalRenderer();
        
        System.out.println("Starting FastTerminal 0.1.2 Minimal Core Demo...");
        
        // Simple loop
        for (int i = 0; i < 100; i++) {
            scene.clear();
            scene.writeAscii(i % 60, 10, "FastTerminal 0.1.2 Minimal Core", 255, 0);
            scene.writeAscii(10, 12, "No UI components used! Just raw grid rendering.", 100, 0);
            
            renderer.render(scene);
            Thread.sleep(50);
        }
        
        System.out.println("Demo finished.");
    }
}
