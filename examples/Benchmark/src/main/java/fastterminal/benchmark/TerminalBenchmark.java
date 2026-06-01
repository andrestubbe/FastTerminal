package fastterminal.benchmark;

import fastterminal.FastTerminalRenderer;
import fastterminal.FastTerminalScene;
import org.openjdk.jmh.annotations.*;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class TerminalBenchmark {

    private FastTerminalRenderer renderer;
    private FastTerminalScene scene;
    private int counter = 0;

    @Setup(Level.Trial)
    public void setup() {
        // Redirect System.out to a NullOutputStream so FastTerminalRenderer doesn't spam the console!
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                // Do nothing (Null bitbucket)
            }
            @Override
            public void write(byte[] b, int off, int len) {
            }
        }));

        // Typical full-screen terminal size
        int width = 120;
        int height = 30;
        
        renderer = new FastTerminalRenderer(width, height);
        // Suppress writing directly to System.out for benchmarking speed
        // Actually FastTerminalRenderer buffers internally into a StringBuilder, 
        // we just don't flush it if we want to measure purely the rendering math.
        // Wait, FastTerminalRenderer flushes to System.out on flushOutput().
        // To benchmark CPU performance without stdout locking, we will just call the internal rendering.
        // render() flushes, so we will benchmark render() directly. It's ok, System.out might bottleneck.
        // If we want pure math, we should benchmark compositeScenes(). But render() does it all.
        
        // FastTerminalScene(x, y, width, height)
        scene = new FastTerminalScene(0, 0, width, height);
        
        // Fill the scene with some data to make the renderer work
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // writeCell(col, row, codepoint, fg, bg)
                scene.writeCell(x, y, '#', -1, -1);
            }
        }
        
        renderer.addScene(scene);
    }

    @Benchmark
    public void benchmarkFullRedraw() {
        // Renders the entire screen from scratch (forces generation of all CSI escape sequences)
        renderer.renderAbsolute();
    }

    @Benchmark
    public void benchmarkDiffRender() {
        // Modify a few characters to trigger diff updates
        counter++;
        scene.writeCell(10, 10, 'A' + (counter % 26), -1, -1);
        scene.writeCell(11, 10, 'A' + ((counter+1) % 26), -1, -1);
        
        // render() will compute diffs and only output the changes
        renderer.render();
    }
}
