package fastterminal.composable;
import fastterminal.component.Component;
import fastterminal.component.Panel;

import fastterminal.FastTerminalScene;
import java.util.List;
import java.util.function.Consumer;

/**
 * A beautiful, fully interactive dropdown selector that expands/collapses dynamically
 * and triggers action callbacks upon option selection.
 */
public class Dropdown extends Component {
    private List<String> items;
    private int selectedIndex = 0;
    private boolean expanded = false;
    private Consumer<Integer> onSelect;

    private int normalBg = 0x27272A; // Charcoal zinc
    private int hoverBg = 0x3F3F46;  // Highlighted gray
    private int itemHoverBg = 0x0EA5E9; // Glowing Sky Blue for item selection
    private int hoveredItemIndex = -1;

    public Dropdown(int x, int y, int width, List<String> items, Consumer<Integer> onSelect) {
        super(x, y, width, 1);
        this.items = items;
        this.onSelect = onSelect;
    }

    @Override
    public boolean contains(int cellX, int cellY) {
        if (expanded) {
            return cellX >= x && cellX < x + width && cellY >= y && cellY < y + 1 + items.size();
        }
        return cellX >= x && cellX < x + width && cellY >= y && cellY < y + 1;
    }

    @Override
    public void handleMouseMove(int cellX, int cellY) {
        super.handleMouseMove(cellX, cellY);

        if (expanded && contains(cellX, cellY)) {
            int relativeRow = cellY - y;
            if (relativeRow >= 1 && relativeRow <= items.size()) {
                hoveredItemIndex = relativeRow - 1;
            } else {
                hoveredItemIndex = -1;
            }
        } else {
            hoveredItemIndex = -1;
        }
    }

    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible) return;

        // 1. Render main collapsed header button
        int headerBg = isHovered ? hoverBg : normalBg;
        for (int c = x; c < x + width; c++) {
            if (c >= 0 && c < canvas.getWidth() && y >= 0 && y < canvas.getHeight()) {
                canvas.writeCell(c, y, ' ', fgColor, headerBg);
            }
        }

        String label = items.get(selectedIndex) + " ▾";
        if (label.length() > width - 2) {
            label = label.substring(0, width - 4) + ".. ▾";
        }
        int textX = x + 1;
        for (int i = 0; i < label.length(); i++) {
            int cx = textX + i;
            if (cx >= x && cx < x + width && cx < canvas.getWidth() && y >= 0 && y < canvas.getHeight()) {
                canvas.writeCell(cx, y, label.charAt(i), fgColor, headerBg);
            }
        }

        // 2. Render expanded dropdown options
        if (expanded) {
            for (int i = 0; i < items.size(); i++) {
                int iy = y + 1 + i;
                if (iy < 0 || iy >= canvas.getHeight()) continue;

                int bg = (hoveredItemIndex == i) ? itemHoverBg : 0x18181B; // Obsidian backdrop
                int fg = (hoveredItemIndex == i) ? 0x000000 : 0xD4D4D8; // Black text on hovered sky-blue

                for (int c = x; c < x + width; c++) {
                    if (c >= 0 && c < canvas.getWidth()) {
                        canvas.writeCell(c, iy, ' ', fg, bg);
                    }
                }

                String itemText = items.get(i);
                if (itemText.length() > width - 2) {
                    itemText = itemText.substring(0, width - 2);
                }
                for (int c = 0; c < itemText.length(); c++) {
                    int cx = x + 1 + c;
                    if (cx >= x && cx < x + width && cx < canvas.getWidth()) {
                        canvas.writeCell(cx, iy, itemText.charAt(c), fg, bg);
                    }
                }
            }
        }
    }

    @Override
    public boolean handleMouseClick(int cellX, int cellY, boolean isPressed) {
        if (!isPressed) return false;

        if (contains(cellX, cellY)) {
            if (expanded) {
                int relativeRow = cellY - y;
                if (relativeRow == 0) {
                    expanded = false;
                } else if (relativeRow >= 1 && relativeRow <= items.size()) {
                    selectedIndex = relativeRow - 1;
                    expanded = false;
                    if (onSelect != null) {
                        onSelect.accept(selectedIndex);
                    }
                }
            } else {
                expanded = true;
            }
            return true;
        } else {
            expanded = false;
        }
        return false;
    }

    public int getSelectedIndex() { return selectedIndex; }
    public void setSelectedIndex(int selectedIndex) { this.selectedIndex = selectedIndex; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
}
