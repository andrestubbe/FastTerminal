package fastterminal.composable;
import fastterminal.component.Component;
import fastterminal.component.Panel;

import fastterminal.FastTerminalScene;

public class PercentageLabel {
    private int fgColor;
    private int bgColor;

    public PercentageLabel(int fgColor, int bgColor) {
        this.fgColor = fgColor;
        this.bgColor = bgColor;
    }

    /**
     * Draws the percentage label into the scene.
     * @return The number of characters written.
     */
    public int draw(FastTerminalScene scene, int col, int row, int percent) {
        String label = String.format("%3d", percent);
        scene.writeString(col, row, label, fgColor, bgColor);
        return label.length();
    }
}
