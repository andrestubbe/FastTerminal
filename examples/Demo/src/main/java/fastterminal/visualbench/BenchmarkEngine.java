package fastterminal.visualbench;

import java.util.Random;

public class BenchmarkEngine {
    public final int NUM_NUMBERS = 2000;
    public final int NUM_UTF8 = 2000;
    
    public final int[][] numbers;
    public final int[][] utf8;
    
    public final int numFrames;
    
    public BenchmarkEngine(int frames) {
        this.numFrames = frames;
        this.numbers = new int[frames][NUM_NUMBERS];
        this.utf8 = new int[frames][NUM_UTF8];
        
        Random rand = new Random(42);
        for (int i = 0; i < frames; i++) {
            for (int j = 0; j < NUM_NUMBERS; j++) {
                this.numbers[i][j] = rand.nextInt(1000000);
            }
            for (int j = 0; j < NUM_UTF8; j++) {
                // Random block character (e.g. █ ░ ▒ ▓)
                this.utf8[i][j] = 0x2588 - rand.nextInt(4);
            }
        }
    }
}
