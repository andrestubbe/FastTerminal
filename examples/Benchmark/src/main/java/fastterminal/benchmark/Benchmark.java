package fastterminal.benchmark;

import fastterminal.FastTerminalRenderer;
import fastterminal.FastTerminalScene;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class Benchmark {

    private FastTerminalRenderer renderer;
    private FastTerminalScene scene;
    private int counter = 0;

    @Setup(Level.Trial)
    public void setup() {
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
            @Override
            public void write(byte[] b, int off, int len) {
            }
        }));

        int width = 120;
        int height = 30;
        
        renderer = new FastTerminalRenderer(width, height);
        scene = new FastTerminalScene(0, 0, width, height);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                scene.writeCell(x, y, '#', -1, -1);
            }
        }
        
        renderer.addScene(scene);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void benchmarkFullRedraw() {
        renderer.renderAbsolute();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void benchmarkDiffRender() {
        counter++;
        scene.writeCell(10, 10, 'A' + (counter % 26), -1, -1);
        scene.writeCell(11, 10, 'A' + ((counter + 1) % 26), -1, -1);
        renderer.render();
    }
}
