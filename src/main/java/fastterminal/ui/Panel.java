package fastterminal.ui;

import fastterminal.FastTerminalScene;
import fastemojis.FastEmojis;
import java.util.ArrayList;
import java.util.List;

/**
 * A beautiful solid-colored borderless container panel with a clean 3D drop-shadow.
 * Supports premium border styles (rounded, single, double) and top-aligned panel titles.
 * Acts as a true container for child components.
 */
public class Panel extends Component {
    
    public enum BorderStyle {
        NONE,
        SINGLE,
        ROUNDED,
        DOUBLE
    }

    private boolean hasShadow = true;
    private BorderStyle borderStyle = BorderStyle.NONE;
    private String title = null;
    private int borderFg = 0x64748B; // Default slate gray border
    
    private final List<Component> children = new ArrayList<>();

    public Panel(int x, int y, int width, int height, int bgColor) {
        super(x, y, width, height);
        this.bgColor = bgColor;
    }

    /**
     * Adds a child component relative to this panel's coordinates.
     */
    public void add(Component child) {
        child.setX(this.x + child.getX());
        child.setY(this.y + child.getY());
        children.add(child);
    }

    @Override
    public void setX(int newX) {
        int dx = newX - this.x;
        super.setX(newX);
        for (Component child : children) {
            child.setX(child.getX() + dx);
        }
    }

    @Override
    public void setY(int newY) {
        int dy = newY - this.y;
        super.setY(newY);
        for (Component child : children) {
            child.setY(child.getY() + dy);
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible) return;

        // 1. Draw modern Lanterna-style drop shadow (2-cols right, 1-row bottom)
        if (hasShadow) {
            int shadowBg = 0x05070A; // Extremely deep transparent shadow charcoal
            
            // Bottom shadow (offset y+1)
            int sy = y + height;
            for (int sx = x + 2; sx < x + width + 2; sx++) {
                if (sx >= 0 && sx < canvas.getWidth() && sy >= 0 && sy < canvas.getHeight()) {
                    canvas.writeCell(sx, sy, ' ', 0x1E293B, shadowBg);
                }
            }
            // Right shadow (offset x+width)
            for (int sx = x + width; sx < x + width + 2; sx++) {
                for (int ty = y + 1; ty < y + height; ty++) {
                    if (sx >= 0 && sx < canvas.getWidth() && ty >= 0 && ty < canvas.getHeight()) {
                        canvas.writeCell(sx, ty, ' ', 0x1E293B, shadowBg);
                    }
                }
            }
        }

        // 2. Draw solid-colored background
        for (int r = y; r < y + height; r++) {
            for (int c = x; c < x + width; c++) {
                if (c >= 0 && c < canvas.getWidth() && r >= 0 && r < canvas.getHeight()) {
                    canvas.writeCell(c, r, ' ', fgColor, bgColor);
                }
            }
        }

        // 2.5. Draw premium borders if specified
        if (borderStyle != BorderStyle.NONE) {
            String horiz = FastEmojis.BOX_HORIZONTAL;
            String vert = FastEmojis.BOX_VERTICAL;
            String tl = FastEmojis.BOX_TOP_LEFT;
            String tr = FastEmojis.BOX_TOP_RIGHT;
            String bl = FastEmojis.BOX_BOTTOM_LEFT;
            String br = FastEmojis.BOX_BOTTOM_RIGHT;

            if (borderStyle == BorderStyle.ROUNDED) {
                tl = FastEmojis.BOX_ROUND_TOP_LEFT;
                tr = FastEmojis.BOX_ROUND_TOP_RIGHT;
                bl = FastEmojis.BOX_ROUND_BOTTOM_LEFT;
                br = FastEmojis.BOX_ROUND_BOTTOM_RIGHT;
            } else if (borderStyle == BorderStyle.DOUBLE) {
                horiz = FastEmojis.BOX_DOUBLE_HORIZONTAL;
                vert = FastEmojis.BOX_DOUBLE_VERTICAL;
                tl = FastEmojis.BOX_DOUBLE_TOP_LEFT;
                tr = FastEmojis.BOX_DOUBLE_TOP_RIGHT;
                bl = FastEmojis.BOX_DOUBLE_BOTTOM_LEFT;
                br = FastEmojis.BOX_DOUBLE_BOTTOM_RIGHT;
            }

            int hCp = horiz.codePointAt(0);
            int vCp = vert.codePointAt(0);
            int tlCp = tl.codePointAt(0);
            int trCp = tr.codePointAt(0);
            int blCp = bl.codePointAt(0);
            int brCp = br.codePointAt(0);

            // Draw horizontal borders
            for (int c = x + 1; c < x + width - 1; c++) {
                canvas.writeCell(c, y, hCp, borderFg, bgColor);
                canvas.writeCell(c, y + height - 1, hCp, borderFg, bgColor);
            }
            // Draw vertical borders
            for (int r = y + 1; r < y + height - 1; r++) {
                canvas.writeCell(x, r, vCp, borderFg, bgColor);
                canvas.writeCell(x + width - 1, r, vCp, borderFg, bgColor);
            }
            // Draw corners
            canvas.writeCell(x, y, tlCp, borderFg, bgColor);
            canvas.writeCell(x + width - 1, y, trCp, borderFg, bgColor);
            canvas.writeCell(x, y + height - 1, blCp, borderFg, bgColor);
            canvas.writeCell(x + width - 1, y + height - 1, brCp, borderFg, bgColor);

            // Centered panel title rendering
            if (title != null && !title.isEmpty()) {
                String formattedTitle = " " + title + " ";
                int titleX = x + (width - formattedTitle.length()) / 2;
                if (titleX > x) {
                    canvas.writeString(titleX, y, formattedTitle, borderFg, bgColor);
                }
            }
        }

        // 3. Render all child components
        for (Component child : children) {
            child.render(canvas);
        }
    }

    @Override
    public void handleMouseMove(int cellX, int cellY) {
        super.handleMouseMove(cellX, cellY);
        // Forward to children
        for (Component child : children) {
            child.handleMouseMove(cellX, cellY);
        }
    }

    @Override
    public boolean handleMouseClick(int cellX, int cellY, boolean isPressed) {
        boolean handled = false;
        // Forward click events to children in reverse order (top-most component gets click first!)
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).handleMouseClick(cellX, cellY, isPressed)) {
                handled = true;
                break;
            }
        }
        
        if (!handled) {
            handled = super.handleMouseClick(cellX, cellY, isPressed);
        }
        return handled;
    }

    public List<Component> getChildren() { return children; }
    public boolean isHasShadow() { return hasShadow; }
    public void setHasShadow(boolean hasShadow) { this.hasShadow = hasShadow; }
    
    public BorderStyle getBorderStyle() { return borderStyle; }
    public void setBorderStyle(BorderStyle borderStyle) { this.borderStyle = borderStyle; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getBorderFg() { return borderFg; }
    public void setBorderFg(int borderFg) { this.borderFg = borderFg; }
}
