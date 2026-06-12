package fastterminal.visualbench;

public class VisualBenchmark {
    public static void main(String[] args) throws InterruptedException {
        // Clear screen fully at start
        System.out.print("\033[2J\033[H");
        
        System.out.println("[INFO] Generating Benchmark Data (10,000 frames)...");
        BenchmarkEngine engine = new BenchmarkEngine(10000);
        
        System.out.println("[INFO] Data ready. Starting visualizer...");
        Thread.sleep(1000);
        System.out.print("\033[2J");
        
        VisualizerCombined visualizer = new VisualizerCombined();
        
        int frameSb = 0;
        int frameFa = 0;
        
        while (true) {
            // Measure StringBuilder
            long startSb = System.nanoTime();
            int fpsSb = 0;
            // Run for ~500ms
            while (System.nanoTime() - startSb < 500_000_000L) {
                RendererStringBuilder.run(engine, frameSb % engine.numFrames);
                frameSb++;
                fpsSb++;
            }
            long timeSb = System.nanoTime() - startSb;
            double msSb = (timeSb / 1_000_000.0) / fpsSb;
            fpsSb *= 2; // scale to 1 second
            
            // Measure FastASCII
            long startFa = System.nanoTime();
            int fpsFa = 0;
            // Run for ~500ms
            while (System.nanoTime() - startFa < 500_000_000L) {
                RendererFastASCII.run(engine, frameFa % engine.numFrames);
                frameFa++;
                fpsFa++;
            }
            long timeFa = System.nanoTime() - startFa;
            double msFa = (timeFa / 1_000_000.0) / fpsFa;
            fpsFa *= 2; // scale to 1 second
            
            visualizer.update(fpsSb, fpsFa, msSb, msFa);
            
            // Let the terminal breathe
            Thread.sleep(16);
        }
    }
}
