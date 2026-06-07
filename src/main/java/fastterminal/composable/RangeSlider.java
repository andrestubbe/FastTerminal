package fastterminal.composable;

import fastterminal.FastTerminalScene;
import fastterminal.component.Component;

public class RangeSlider extends Component {
    private double valueMin = 0.25; // 0.0 to 1.0
    private double valueMax = 0.75; // 0.0 to 1.0
    private SliderStyle style = SliderStyle.BLOCK;
    private int accentColor = 0x10B981; // Emerald-500
    private int trackColor = 0x333333; // Dark gray
    
    private boolean draggingMin = false;
    private boolean draggingMax = false;

    public RangeSlider(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void setStyle(SliderStyle style) { this.style = style; }
    public void setAccentColor(int color) { this.accentColor = color; }
    public void setTrackColor(int color) { this.trackColor = color; }
    
    public double getMin() { return valueMin; }
    public double getMax() { return valueMax; }
    
    public void setValues(double min, double max) {
        this.valueMin = Math.max(0.0, Math.min(1.0, min));
        this.valueMax = Math.max(0.0, Math.min(1.0, max));
        if (this.valueMin > this.valueMax) this.valueMin = this.valueMax;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible) return;

        int fillMin = (int) Math.round(valueMin * (width - 1));
        int fillMax = (int) Math.round(valueMax * (width - 1));
        
        for (int c = 0; c < width; c++) {
            char ch;
            int fg, bg;
            
            boolean inRange = (c >= fillMin && c <= fillMax);
            
            switch (style) {
                case BLOCK:
                    if (c == fillMin || c == fillMax) {
                        ch = '█'; fg = accentColor; bg = trackColor;
                    } else if (inRange) {
                        ch = '▒'; fg = accentColor; bg = trackColor;
                    } else {
                        ch = '░'; fg = trackColor; bg = -1;
                    }
                    break;
                case LINE:
                    if (c == fillMin || c == fillMax) {
                        ch = '●'; fg = accentColor; bg = -1;
                    } else if (inRange) {
                        ch = '━'; fg = accentColor; bg = -1;
                    } else {
                        ch = '─'; fg = trackColor; bg = -1;
                    }
                    break;
                case RETRO:
                default:
                    if (c == fillMin || c == fillMax) {
                        ch = 'þ'; fg = accentColor; bg = -1;
                    } else if (inRange) {
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
            if (isPressed) {
                double val = getValueFromMouse(cellX);
                if (Math.abs(val - valueMin) < Math.abs(val - valueMax)) {
                    draggingMin = true;
                    draggingMax = false;
                    setValues(val, valueMax);
                } else {
                    draggingMin = false;
                    draggingMax = true;
                    setValues(valueMin, val);
                }
            } else {
                draggingMin = false;
                draggingMax = false;
            }
            return true;
        } else {
            if (!isPressed) {
                draggingMin = false;
                draggingMax = false;
            }
        }
        return false;
    }

    @Override
    public void handleMouseDrag(int cellX, int cellY) {
        if (draggingMin || draggingMax) {
            double val = getValueFromMouse(Math.max(x, Math.min(x + width - 1, cellX)));
            if (draggingMin) {
                setValues(val, valueMax);
            } else {
                setValues(valueMin, val);
            }
        }
    }

    private double getValueFromMouse(int cellX) {
        int relativeX = cellX - x;
        if (width <= 1) return 0;
        return Math.max(0.0, Math.min(1.0, (double) relativeX / (width - 1)));
    }
}
