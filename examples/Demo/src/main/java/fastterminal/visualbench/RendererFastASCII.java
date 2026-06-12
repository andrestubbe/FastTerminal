package fastterminal.visualbench;

import fastascii.FastASCIIWriter;

public class RendererFastASCII {
    private static final byte[] buffer = new byte[1024 * 128]; // reusable buffer
    
    public static long run(BenchmarkEngine engine, int frameIndex) {
        int offset = 0;
        
        for (int j = 0; j < engine.NUM_NUMBERS; j++) {
            offset += FastASCIIWriter.writeInt(buffer, offset, engine.numbers[frameIndex][j]);
            buffer[offset++] = ' ';
        }
        for (int j = 0; j < engine.NUM_UTF8; j++) {
            offset += FastASCIIWriter.writeUtf8(buffer, offset, engine.utf8[frameIndex][j]);
            buffer[offset++] = ' ';
        }
        buffer[offset++] = '\n';
        
        return offset;
    }
}
