package fastterminal.composable;
import fastterminal.component.Component;
import fastterminal.component.Panel;

import fastterminal.FastTerminalScene;

public class ProgressBar {
    private int filledColor;
    private int emptyColor;

    public ProgressBar(int filledColor, int emptyColor) {
        this.filledColor = filledColor;
        this.emptyColor = emptyColor;
    }

    public void draw(FastTerminalScene scene, int col, int row, int width, int percent) {
        int filled = (width * percent) / 100;
        String filledBar = repeat('█', filled);
        String emptyBar = repeat('░', width - filled);
        
        scene.writeString(col, row, filledBar, filledColor, -1);
        scene.writeString(col + filled, row, emptyBar, emptyColor, -1);
    }

    private String repeat(char ch, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
}
