package fastterminal.component;
import fastterminal.layout.Layout;

import fastterminal.FastTerminalScene;

public class Box {
    private int color;

    public Box(int color) {
        this.color = color;
    }

    public void draw(FastTerminalScene s, int x, int y, int w, int h) {
        for (int i = 0; i < w; i++) {
            s.writeCell(x + i, y, '─', color, -1);
            s.writeCell(x + i, y + h - 1, '─', color, -1);
        }

        for (int i = 0; i < h; i++) {
            s.writeCell(x, y + i, '│', color, -1);
            s.writeCell(x + w - 1, y + i, '│', color, -1);
        }

        s.writeCell(x, y, '┌', color, -1);
        s.writeCell(x + w - 1, y, '┐', color, -1);
        s.writeCell(x, y + h - 1, '└', color, -1);
        s.writeCell(x + w - 1, y + h - 1, '┘', color, -1);
    }
}
