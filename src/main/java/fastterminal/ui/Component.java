package fastterminal.ui;

import fastterminal.FastTerminalScene;

/**
 * Base class for all high-performance TUI components in FastTerminal.
 */
public abstract class Component {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean visible = true;
    protected int bgColor = 0x0E1726; // Slate default
    protected int fgColor = 0xFFFFFF; // White default
    protected boolean isHovered = false;

    public Component(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(FastTerminalScene canvas);

    public boolean contains(int cellX, int cellY) {
        return cellX >= x && cellX < x + width && cellY >= y && cellY < y + height;
    }

    public void handleMouseMove(int cellX, int cellY) {
        boolean previouslyHovered = isHovered;
        isHovered = contains(cellX, cellY);
        if (previouslyHovered != isHovered) {
            onHoverChanged(isHovered);
        }
    }

    public boolean handleMouseClick(int cellX, int cellY, boolean isPressed) {
        if (contains(cellX, cellY)) {
            if (isPressed) {
                onPress();
            } else {
                onRelease();
            }
            return true;
        }
        return false;
    }

    protected void onHoverChanged(boolean hovered) {}
    protected void onPress() {}
    protected void onRelease() {}

    // Getters and Setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public int getBgColor() { return bgColor; }
    public void setBgColor(int bgColor) { this.bgColor = bgColor; }
    public int getFgColor() { return fgColor; }
    public void setFgColor(int fgColor) { this.fgColor = fgColor; }
    public boolean isHovered() { return isHovered; }
}
