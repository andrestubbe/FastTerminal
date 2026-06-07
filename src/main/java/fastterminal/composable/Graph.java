package fastterminal.composable;
import fastterminal.component.Component;
import fastterminal.component.Panel;

import fastterminal.FastTerminalScene;

public class Graph {

    private static final char[] BLOCKS = {
            ' ', '▁', '▂', '▃', '▄', '▅', '▆', '▇', '█'
    };

    private int fgColor;

    public Graph(int fgColor) {
        this.fgColor = fgColor;
    }

    public void draw(FastTerminalScene scene, int col, int row, int width, int height, int[] history) {

        int totalLevels = height * 8;

        for (int x = 0; x < width && x < history.length; x++) {

            int val = history[x];
            int levels = (val * totalLevels) / 100;

            for (int y = 0; y < height; y++) {

                int cellLevel = levels - ((height - 1 - y) * 8);

                if (cellLevel < 0) cellLevel = 0;
                if (cellLevel > 8) cellLevel = 8;

                scene.writeCell(
                        col + x,
                        row + y,
                        BLOCKS[cellLevel],
                        fgColor,
                        -1
                );
            }
        }
    }
}
