package fastterminal.ui;

import fastterminal.FastTerminalScene;

/**
 * A highly-responsive click-sensitive TUI button supporting hover, active states, and custom action callbacks.
 */
public class Button extends Component {
    private String text;
    private Runnable action;
    private int normalBg = 0x27272A; // Charcoal zinc
    private int hoverBg = 0xEAB308;  // Brilliant Gold
    private int activeBg = 0xCA8A04; // Dark gold
    
    private int normalFg = 0xFFFFFF; // White normal text
    private int hoverFg = 0x000000;  // Black hover text
    private int activeFg = 0x000000; // Black active text
    
    private boolean isPressedState = false;

    public Button(int x, int y, int width, int height, String text, Runnable action) {
        super(x, y, width, height);
        this.text = text;
        this.action = action;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible) return;

        int currentBg = normalBg;
        int currentFg = normalFg;
        
        if (isPressedState) {
            currentBg = activeBg;
            currentFg = activeFg;
        } else if (isHovered) {
            currentBg = hoverBg;
            currentFg = hoverFg;
        }

        // Render solid background
        for (int r = y; r < y + height; r++) {
            for (int c = x; c < x + width; c++) {
                if (c >= 0 && c < canvas.getWidth() && r >= 0 && r < canvas.getHeight()) {
                    canvas.writeCell(c, r, ' ', currentFg, currentBg);
                }
            }
        }

        // Render centered text
        int textX = x + (width - text.length()) / 2;
        int textY = y + height / 2;
        if (textX < x) textX = x;

        for (int i = 0; i < text.length(); i++) {
            int cx = textX + i;
            if (cx >= x && cx < x + width && cx < canvas.getWidth() && textY >= 0 && textY < canvas.getHeight()) {
                canvas.writeCell(cx, textY, text.charAt(i), currentFg, currentBg);
            }
        }
    }

    @Override
    protected void onPress() {
        isPressedState = true;
    }

    @Override
    protected void onRelease() {
        if (isPressedState) {
            isPressedState = false;
            if (action != null) {
                action.run();
            }
        }
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public int getNormalBg() { return normalBg; }
    public void setNormalBg(int normalBg) { this.normalBg = normalBg; }
    public int getHoverBg() { return hoverBg; }
    public void setHoverBg(int hoverBg) { this.hoverBg = hoverBg; }
    public int getActiveBg() { return activeBg; }
    public void setActiveBg(int activeBg) { this.activeBg = activeBg; }

    public int getNormalFg() { return normalFg; }
    public void setNormalFg(int normalFg) { this.normalFg = normalFg; }
    public int getHoverFg() { return hoverFg; }
    public void setHoverFg(int hoverFg) { this.hoverFg = hoverFg; }
    public int getActiveFg() { return activeFg; }
    public void setActiveFg(int activeFg) { this.activeFg = activeFg; }
}
