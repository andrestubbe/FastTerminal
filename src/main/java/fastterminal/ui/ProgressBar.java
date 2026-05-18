package fastterminal.ui;

import fastterminal.FastTerminalScene;

/**
 * A beautiful progress bar using block drawing characters for dynamic visualization.
 */
public class ProgressBar extends Component {
    private double progress = 0.0; // Scaled from 0.0 to 1.0
    private int fillBg = 0x10B981;  // Vibrant Emerald Green
    private int emptyBg = 0x1E293B; // Dark Slate gray

    public ProgressBar(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible) return;

        int fillWidth = (int) (progress * width);
        if (fillWidth > width) fillWidth = width;
        if (fillWidth < 0) fillWidth = 0;

        for (int r = y; r < y + height; r++) {
            if (r < 0 || r >= canvas.getHeight()) continue;

            // Render filled bar
            for (int c = x; c < x + fillWidth; c++) {
                if (c >= 0 && c < canvas.getWidth()) {
                    canvas.writeCell(c, r, '█', 0xFFFFFF, fillBg);
                }
            }
            // Render empty tracks
            for (int c = x + fillWidth; c < x + width; c++) {
                if (c >= 0 && c < canvas.getWidth()) {
                    canvas.writeCell(c, r, '░', 0x475569, emptyBg);
                }
            }
        }
    }

    public double getProgress() { return progress; }
    public void setProgress(double progress) {
        this.progress = Math.max(0.0, Math.min(1.0, progress));
    }
    public int getFillBg() { return fillBg; }
    public void setFillBg(int fillBg) { this.fillBg = fillBg; }
}
