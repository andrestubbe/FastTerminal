package fastterminal.visualbench;

import fastterminal.FastTerminal;

public class VisualizerCombined {
    
    // Sparkline characters (from low to high)
    private static final char[] SPARK_CHARS = {' ', '▂', '▃', '▄', '▅', '▆', '▇', '█'};
    
    // History buffers for sparklines
    private static final int HISTORY_SIZE = 40;
    private final double[] historySb = new double[HISTORY_SIZE];
    private final double[] historyFa = new double[HISTORY_SIZE];
    private int historyIdx = 0;
    
    public void update(int fpsSb, int fpsFa, double msSb, double msFa) {
        // Record history
        historySb[historyIdx] = msSb;
        historyFa[historyIdx] = msFa;
        historyIdx = (historyIdx + 1) % HISTORY_SIZE;
        
        // Build the UI frame in a single StringBuilder for atomic printing
        StringBuilder ui = new StringBuilder(4096);
        
        // Hide cursor and move to top-left
        ui.append("\033[?25l\033[H");
        
        // Colors
        String C_RESET = "\033[0m";
        String C_RED   = "\033[38;2;255;50;50m";
        String C_GREEN = "\033[38;2;50;255;100m";
        String C_DIM   = "\033[38;2;100;100;100m";
        String C_WHITE = "\033[38;2;255;255;255m";
        
        ui.append(C_WHITE).append("=========================================================================\n");
        ui.append(" 🧨 FastASCII Visual Benchmark\n");
        ui.append("=========================================================================\n\n");
        
        // --- StringBuilder Section ---
        ui.append(C_RED).append(" StringBuilder + String.getBytes() ").append(C_DIM).append("(Legacy)\n");
        ui.append(C_RESET).append(" ┌─────────────────────────────────────────────────────────┐\n");
        ui.append(" │ FPS: ").append(String.format("%-10d", fpsSb)).append(" | Frame Time: ").append(String.format("%6.2f", msSb)).append(" ms               │\n");
        ui.append(" ├─────────────────────────────────────────────────────────┤\n");
        
        // Bar graph
        int barSb = Math.min(50, (int)(msSb / 2.0)); 
        ui.append(" │ ").append(C_RED);
        for(int i=0; i<barSb; i++) ui.append("█");
        for(int i=barSb; i<50; i++) ui.append(" ");
        ui.append(C_RESET).append("       │\n");
        
        // Sparkline
        ui.append(" │ History: ").append(C_RED);
        ui.append(buildSparkline(historySb));
        ui.append(C_RESET).append("                 │\n");
        
        ui.append(" └─────────────────────────────────────────────────────────┘\n\n");
        
        // --- FastASCII Section ---
        ui.append(C_GREEN).append(" FastASCII.writeInt() + writeUtf8() ").append(C_DIM).append("(Zero Allocation)\n");
        ui.append(C_RESET).append(" ┌─────────────────────────────────────────────────────────┐\n");
        ui.append(" │ FPS: ").append(String.format("%-10d", fpsFa)).append(" | Frame Time: ").append(String.format("%6.2f", msFa)).append(" ms               │\n");
        ui.append(" ├─────────────────────────────────────────────────────────┤\n");
        
        // Bar graph
        int barFa = Math.min(50, (int)(msFa / 2.0)); 
        ui.append(" │ ").append(C_GREEN);
        for(int i=0; i<barFa; i++) ui.append("█");
        for(int i=barFa; i<50; i++) ui.append(" ");
        ui.append(C_RESET).append("       │\n");
        
        // Sparkline
        ui.append(" │ History: ").append(C_GREEN);
        ui.append(buildSparkline(historyFa));
        ui.append(C_RESET).append("                 │\n");
        
        ui.append(" └─────────────────────────────────────────────────────────┘\n\n");
        
        ui.append(C_DIM).append(" Press Ctrl+C to exit.").append(C_RESET).append("\n");
        
        // Flush to terminal
        System.out.print(ui.toString());
    }
    
    private String buildSparkline(double[] hist) {
        // Find max in history to scale
        double max = 0;
        for (double v : hist) if (v > max) max = v;
        if (max == 0) max = 1;
        
        StringBuilder sb = new StringBuilder(HISTORY_SIZE);
        for (int i = 0; i < HISTORY_SIZE; i++) {
            int idx = (historyIdx + i) % HISTORY_SIZE;
            double val = hist[idx];
            if (val == 0) {
                sb.append(' ');
                continue;
            }
            int charIdx = (int) ((val / max) * (SPARK_CHARS.length - 1));
            charIdx = Math.max(0, Math.min(SPARK_CHARS.length - 1, charIdx));
            sb.append(SPARK_CHARS[charIdx]);
        }
        return sb.toString();
    }
}
