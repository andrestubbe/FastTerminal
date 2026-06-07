package fastterminal.composable;

import fastterminal.FastTerminalScene;
import fastterminal.component.Component;

public class Slider extends Component {
    private double value = 0.5; // 0.0 to 1.0
    private SliderStyle style = SliderStyle.BLOCK;
    private int accentColor = 0x3B82F6; // Blue-500
    private int trackColor = 0x333333; // Dark gray

    public Slider(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void setStyle(SliderStyle style) { this.style = style; }
    public void setAccentColor(int color) { this.accentColor = color; }
    public void setTrackColor(int color) { this.trackColor = color; }
    public double getValue() { return value; }
    public void setValue(double v) { this.value = Math.max(0.0, Math.min(1.0, v)); }

    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible) return;

        int fillWidth = (int) Math.round(value * (width - 1));
        
        for (int c = 0; c < width; c++) {
            char ch;
            int fg, bg;
            
            switch (style) {
                case BLOCK:
                    if (c < fillWidth) {
                        ch = '█'; fg = accentColor; bg = trackColor;
                    } else if (c == fillWidth) {
                        ch = '█'; fg = accentColor; bg = trackColor;
                    } else {
                        ch = '░'; fg = trackColor; bg = -1;
                    }
                    break;
                case LINE:
                    if (c == fillWidth) {
                        ch = '●'; fg = accentColor; bg = -1;
                    } else if (c < fillWidth) {
                        ch = '━'; fg = accentColor; bg = -1;
                    } else {
                        ch = '─'; fg = trackColor; bg = -1;
                    }
                    break;
                case RETRO:
                default:
                    if (c == fillWidth) {
                        ch = 'þ'; fg = accentColor; bg = -1;
                    } else if (c < fillWidth) {
                        ch = 'Í'; fg = accentColor; bg = -1;
                    } else {
                        ch = 'Ä'; fg = trackColor; bg = -1;
                    }
                    break;
            }
            
            canvas.writeCell(x + c, y + (height / 2), ch, fg, bg);
        }
    }

    @Override
    public boolean handleMouseClick(int cellX, int cellY, boolean isPressed) {
        if (contains(cellX, cellY)) {
            updateValueFromMouse(cellX);
            return true;
        }
        return false;
    }

    @Override
    public void handleMouseDrag(int cellX, int cellY) {
        if (contains(cellX, cellY)) {
            updateValueFromMouse(cellX);
        } else {
            // Allow dragging slightly outside bounds horizontally
            if (cellY >= y && cellY < y + height) {
                if (cellX < x) setValue(0.0);
                if (cellX >= x + width) setValue(1.0);
            }
        }
    }

    private void updateValueFromMouse(int cellX) {
        int relativeX = cellX - x;
        if (width <= 1) return;
        setValue((double) relativeX / (width - 1));
    }
}
