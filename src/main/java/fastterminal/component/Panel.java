package fastterminal.component;

import fastterminal.FastTerminalScene;
import fastemojis.FastEmojis;
import fastterminal.composable.Button;
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
    private int borderFg = 0x64748B;
    
    private boolean hasHeaderBar = false;
    private int headerBg = 0xFFFFFF;
    private int headerFg = 0x18181B;
    private boolean showWindowButtons = true;
    private double bodyAlpha = 1.0; // 1.0 = fully opaque, 0.95 = 95% opaque (5% transparent)

    private int shadowBg = 0x000000; // Pure black background for realistic transparent drop shadow
    private int shadowFg = 0x000000; // Pure black foreground for realistic transparent drop shadow
    private double shadowAlpha = 0.25; // Default 25% opacity shadow
    private boolean hasResizeButton = true; // Toggle for bottom-right corner resize button
    private Button resizeButton;            // The interactive resize button component
    private Button closeButton;             // The interactive close button component
    private Button minimizeButton;          // The interactive minimize button component

    // Minimize state
    private boolean isMinimized = false;
    private int restoredHeight = -1; // Saved height before minimizing

    // BeOS pinstripe header: true = draw horizontal pinstripe pattern on the title row
    private boolean beosStyle = false;

    private final List<Component> children = new ArrayList<>();

    public Panel(int x, int y, int width, int height, int bgColor) {
        super(x, y, width, height);
        this.bgColor = bgColor;

        // Initialize the interactive resize button component at the bottom-right corner
        this.resizeButton = new Button(width - 1, height - 1, 1, 1, "◢", null);
        this.resizeButton.setNormalBg(bgColor);
        this.resizeButton.setHoverBg(0xE2E8F0);  // Clean zinc/slate gray on hover
        this.resizeButton.setActiveBg(0x94A3B8); // Deeper active click slate gray
        this.resizeButton.setNormalFg(0x94A3B8); // slate-400 normal text color
        this.resizeButton.setHoverFg(0x0F172A);  // slate-900 hover text color
        this.resizeButton.setActiveFg(0x0F172A); // slate-900 active text color
        this.resizeButton.setVisible(hasResizeButton);
        add(this.resizeButton);

        // Close button: 3 chars wide, flush to right edge
        this.closeButton = new Button(width - 3, 0, 3, 1, " ✕ ", () -> System.exit(0));
        this.closeButton.setNormalBg(0xEF4444);  // Vibrant solid red background
        this.closeButton.setNormalFg(0xFFFFFF);  // Crisp white symbol
        this.closeButton.setHoverBg(0xDC2626);   // Deeper crimson red on hover
        this.closeButton.setHoverFg(0xFFFFFF);   // Crisp white symbol on hover
        this.closeButton.setActiveBg(0x991B1B);  // Dark crimson red on press
        this.closeButton.setActiveFg(0xFFFFFF);  // Crisp white symbol on active click
        this.closeButton.setVisible(showWindowButtons && hasHeaderBar);
        add(this.closeButton);

        // Minimize button: 3 chars wide, adjacent to close button
        this.minimizeButton = new Button(width - 6, 0, 3, 1, " _ ", () -> toggleMinimize());
        this.minimizeButton.setNormalBg(0xD9C676);  // Darker yellow matching titlebar
        this.minimizeButton.setNormalFg(0x000000);  // Black symbol
        this.minimizeButton.setHoverBg(0xC9B55F);   // Darker version on hover
        this.minimizeButton.setHoverFg(0x000000);   // Black on hover
        this.minimizeButton.setActiveBg(0xB09F48);  // Even darker on press
        this.minimizeButton.setActiveFg(0x000000);  // Black on active
        this.minimizeButton.setVisible(showWindowButtons && hasHeaderBar);
        add(this.minimizeButton);
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

    /**
     * Toggles the visibility of the interactive resize button.
     */
    public void setHasResizeButton(boolean hasResizeButton) {
        this.hasResizeButton = hasResizeButton;
        if (this.resizeButton != null) {
            this.resizeButton.setVisible(hasResizeButton);
        }
    }

    /**
     * Toggles the panel between fully hidden (desktop icon) and restored state.
     */
    public void toggleMinimize() {
        if (isMinimized) {
            // Restore: make panel visible again at its original size
            isMinimized = false;
            this.visible = true;
            if (restoredHeight > 1) {
                super.setHeight(restoredHeight);
                for (Component child : children) {
                    if (child != resizeButton && child != closeButton && child != minimizeButton) {
                        child.setVisible(true);
                        int relY = child.getY() - this.y;
                        if (relY == 1) {
                            child.setHeight(restoredHeight - 1);
                        }
                    }
                }
                if (resizeButton != null) {
                    resizeButton.setY(this.y + restoredHeight - 1);
                    resizeButton.setVisible(hasResizeButton);
                }
            }
        } else {
            // Minimize: save size and fully hide the panel
            isMinimized = true;
            restoredHeight = this.height;
            this.visible = false;
        }
    }

    /**
     * Returns the pixel width (in cells) of the desktop icon for this panel.
     */
    public int getIconWidth() {
        String label = getIconLabel();
        return label.length() + 2; // 1-cell padding each side
    }

    private String getIconLabel() {
        return " ▢ " + (title != null ? title : "Window") + " ";
    }

    /**
     * Renders the minimized desktop icon at the given screen position.
     * Call this from your render loop when isMinimized() is true.
     */
    public void renderDesktopIcon(FastTerminalScene canvas, int iconX, int iconY) {
        if (!isMinimized) return;
        String label = getIconLabel();
        int iconW = label.length() + 2;
        // Plain darker yellow matching titlebar (no gradient)
        int bg = 0xD9C676;
        for (int i = 0; i < iconW; i++) {
            int cx = iconX + i;
            if (cx >= 0 && cx < canvas.getWidth() && iconY >= 0 && iconY < canvas.getHeight()) {
                char ch = (i >= 1 && i - 1 < label.length()) ? label.charAt(i - 1) : ' ';
                canvas.writeCell(cx, iconY, ch, headerFg, bg);
            }
        }
    }

    /**
     * Returns true if the given cell coordinates hit the desktop icon.
     */
    public boolean isIconHit(int mx, int my, int iconX, int iconY) {
        if (!isMinimized) return false;
        return my == iconY && mx >= iconX && mx < iconX + getIconWidth();
    }

    public boolean isMinimized() { return isMinimized; }

    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible) return;

        // 1. Draw modern Lanterna-style drop shadow (only y+ bottom shadow, no sides, transparently blended!)
        if (hasShadow) {
            // Bottom shadow (offset y+height)
            int sy = y + height;
            for (int sx = x; sx < x + width; sx++) {
                if (sx >= 0 && sx < canvas.getWidth() && sy >= 0 && sy < canvas.getHeight()) {
                    int idx = sy * canvas.getWidth() + sx;
                    int cp = canvas.getCodepointBuffer()[idx];
                    if (cp == '▄') {
                        // For half-block cells, the panel shadow only falls on the top half (background).
                        // So we darken only the background and leave the foreground (bottom half) completely untouched!
                        canvas.writeCellAlpha(sx, sy, cp, shadowFg, shadowBg, 0.0, shadowAlpha);
                    } else {
                        // Fallback for standard cells: blend both foreground and background.
                        canvas.writeCellAlpha(sx, sy, cp, shadowFg, shadowBg, shadowAlpha, shadowAlpha);
                    }
                }
            }
        }

        // 2. Draw solid-colored background (skip body if minimized)
        int finalBgColor = bgColor;
        if (hasHeaderBar && bgColor == headerBg) {
            finalBgColor = blendColor(bgColor, 0x000000, 0.08); // 8% darker
        }

        // Pre-compute gradient endpoints for header (used if beosStyle is true)
        int gradLeft  = 0xD9C676; // darker yellow matching minimized icon
        int gradRight = 0xD9C676; // darker yellow (no gradient)

        for (int r = y; r < y + height; r++) {
            boolean isHeaderRow = (hasHeaderBar && r == y);
            int currentBg = isHeaderRow ? headerBg : finalBgColor;
            int currentFg = isHeaderRow ? headerFg : fgColor;
            for (int c = x; c < x + width; c++) {
                if (c >= 0 && c < canvas.getWidth() && r >= 0 && r < canvas.getHeight()) {
                    if (isHeaderRow && beosStyle) {
                        // Smooth left-to-right gradient across the full header width
                        double t = (width > 1) ? (double)(c - x) / (width - 1) : 0.0;
                        int gradBg = blendColor(gradLeft, gradRight, t);
                        canvas.writeCell(c, r, ' ', currentFg, gradBg);
                    } else if (isHeaderRow) {
                        canvas.writeCell(c, r, ' ', currentFg, currentBg);
                    } else {
                        // Body rows: render with configurable transparency
                        if (bodyAlpha >= 1.0) {
                            canvas.writeCell(c, r, ' ', currentFg, currentBg);
                        } else {
                            canvas.writeCellAlpha(c, r, ' ', currentFg, currentBg, 1.0, bodyAlpha);
                        }
                    }
                }
            }
        }

        // 2.5. Draw premium borders if specified (suppressed when hasHeaderBar is active or minimized)
        if (borderStyle != BorderStyle.NONE && !isMinimized) {
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

            if (!hasHeaderBar) {
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
            } else {
                // Draw bottom border
                for (int c = x + 1; c < x + width - 1; c++) {
                    canvas.writeCell(c, y + height - 1, hCp, borderFg, bgColor);
                }
                // Draw vertical borders
                for (int r = y + 1; r < y + height - 1; r++) {
                    canvas.writeCell(x, r, vCp, borderFg, bgColor);
                    canvas.writeCell(x + width - 1, r, vCp, borderFg, bgColor);
                }
                // Draw bottom corners
                canvas.writeCell(x, y + height - 1, blCp, borderFg, bgColor);
                canvas.writeCell(x + width - 1, y + height - 1, brCp, borderFg, bgColor);
            }
        }

        // Title and buttons are drawn independently of borders when hasHeaderBar is true!
        if (hasHeaderBar) {
            // Left-aligned panel title: render character-by-character to match the gradient bg exactly
            if (title != null && !title.isEmpty()) {
                int titleX = x + 2;
                for (int i = 0; i < title.length(); i++) {
                    int cx = titleX + i;
                    if (cx >= 0 && cx < canvas.getWidth()) {
                        int titleBg;
                        if (beosStyle) {
                            double t = (width > 1) ? (double)(cx - x) / (width - 1) : 0.0;
                            titleBg = blendColor(gradLeft, gradRight, t);
                        } else {
                            titleBg = headerBg;
                        }
                        canvas.writeCell(cx, y, title.charAt(i), headerFg, titleBg);
                    }
                }
            }
        } else {
            // Legacy centered title inside outline border
            if (title != null && !title.isEmpty() && borderStyle != BorderStyle.NONE) {
                String formattedTitle = " " + title + " ";
                int titleX = x + (width - formattedTitle.length()) / 2;
                if (titleX > x) {
                    canvas.writeString(titleX, y, formattedTitle, borderFg, bgColor);
                }
            }
        }

        // 2.7. Dynamically align the normal background of the interactive resize button
        if (resizeButton != null) {
            resizeButton.setNormalBg(finalBgColor);
        }

        // 3. Render content children first, then buttons on top (so buttons always float above content)
        for (Component child : children) {
            if (child != resizeButton && child != closeButton && child != minimizeButton) {
                child.render(canvas);
            }
        }
        // Render buttons last so they paint over any overlapping content
        if (minimizeButton != null) minimizeButton.render(canvas);
        if (closeButton != null) closeButton.render(canvas);
        if (resizeButton != null) resizeButton.render(canvas);
    }

    @Override
    public void handleMouseDrag(int cellX, int cellY) {
        if (!visible) return;
        for (Component child : children) {
            child.handleMouseDrag(cellX, cellY);
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

    private int blendColor(int color1, int color2, double ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 * (1.0 - ratio) + r2 * ratio);
        int g = (int) (g1 * (1.0 - ratio) + g2 * ratio);
        int b = (int) (b1 * (1.0 - ratio) + b2 * ratio);

        return (r << 16) | (g << 8) | b;
    }

    public boolean isCloseClick(int mx, int my) {
        if (!hasHeaderBar || !showWindowButtons) return false;
        return my == y && mx >= x + width - 3 && mx < x + width;
    }

    public boolean isMinimizeClick(int mx, int my) {
        if (!hasHeaderBar || !showWindowButtons) return false;
        return my == y && mx >= x + width - 6 && mx < x + width - 3;
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


    public boolean isResizeClick(int mx, int my) {
        if (!hasResizeButton) return false;
        return mx == x + width - 1 && my == y + height - 1;
    }

    @Override
    public void setWidth(int newWidth) {
        super.setWidth(newWidth);
        if (resizeButton != null) {
            resizeButton.setX(this.x + newWidth - 1);
        }
        if (closeButton != null) {
            closeButton.setX(this.x + newWidth - 3);
        }
        if (minimizeButton != null) {
            minimizeButton.setX(this.x + newWidth - 6);
        }
        // Dynamically resize internal content components to fit content area boundaries
        for (Component child : children) {
            if (child != resizeButton && child != closeButton && child != minimizeButton) {
                int relX = child.getX() - this.x;
                if (relX == 0) {
                    child.setWidth(newWidth);
                } else {
                    child.setWidth(newWidth - 2);
                }
            }
        }
    }

    @Override
    public void setHeight(int newHeight) {
        super.setHeight(newHeight);
        if (resizeButton != null) {
            resizeButton.setY(this.y + newHeight - 1);
        }
        // Dynamically resize internal content components to fit content area boundaries
        for (Component child : children) {
            if (child != resizeButton && child != closeButton && child != minimizeButton) {
                int relY = child.getY() - this.y;
                if (relY == 1) {
                    // Flush child: fill to full height; resize button paints on top
                    child.setHeight(newHeight - 1);
                } else {
                    child.setHeight(newHeight - 2);
                }
            }
        }
    }

    public boolean isHasHeaderBar() { return hasHeaderBar; }
    public void setHasHeaderBar(boolean hasHeaderBar) {
        this.hasHeaderBar = hasHeaderBar;
        if (closeButton != null) {
            closeButton.setVisible(showWindowButtons && hasHeaderBar);
        }
        if (minimizeButton != null) {
            minimizeButton.setVisible(showWindowButtons && hasHeaderBar);
        }
    }
    public int getHeaderBg() { return headerBg; }
    public void setHeaderBg(int headerBg) { this.headerBg = headerBg; }
    public int getHeaderFg() { return headerFg; }
    public void setHeaderFg(int headerFg) { this.headerFg = headerFg; }
    public boolean isShowWindowButtons() { return showWindowButtons; }
    public void setShowWindowButtons(boolean showWindowButtons) {
        this.showWindowButtons = showWindowButtons;
        if (closeButton != null) {
            closeButton.setVisible(showWindowButtons && hasHeaderBar);
        }
        if (minimizeButton != null) {
            minimizeButton.setVisible(showWindowButtons && hasHeaderBar);
        }
    }
    public boolean isBeosStyle() { return beosStyle; }
    public void setBeosStyle(boolean beosStyle) { this.beosStyle = beosStyle; }
    public double getBodyAlpha() { return bodyAlpha; }
    public void setBodyAlpha(double bodyAlpha) { this.bodyAlpha = bodyAlpha; }
    public double getShadowAlpha() { return shadowAlpha; }
    public void setShadowAlpha(double shadowAlpha) { this.shadowAlpha = shadowAlpha; }
    public int getShadowBg() { return shadowBg; }
    public void setShadowBg(int shadowBg) { this.shadowBg = shadowBg; }
    public int getShadowFg() { return shadowFg; }
    public void setShadowFg(int shadowFg) { this.shadowFg = shadowFg; }
}
