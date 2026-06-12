package fastterminal.visualbench;

public class RendererStringBuilder {
    
    public static long run(BenchmarkEngine engine, int frameIndex) {
        StringBuilder sb = new StringBuilder(10000);
        
        for (int j = 0; j < engine.NUM_NUMBERS; j++) {
            sb.append(engine.numbers[frameIndex][j]).append(' ');
        }
        for (int j = 0; j < engine.NUM_UTF8; j++) {
            sb.appendCodePoint(engine.utf8[frameIndex][j]).append(' ');
        }
        sb.append('\n');
        
        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return bytes.length;
    }
}
