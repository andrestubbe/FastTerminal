package fastterminal.composable;
import fastterminal.component.Component;
import fastterminal.component.Panel;

import fastterminal.FastTerminalScene;
import java.util.List;
import java.util.ArrayList;

public class ListBox extends Component {
    private List<String> items;
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    
    public ListBox(int x, int y, int width, int height, List<String> items) {
        super(x, y, width, height);
        this.items = new ArrayList<>(items);
        this.bgColor = 0x222222;
        this.fgColor = 0xFFFFFF;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible) return;
        
        for (int r = 0; r < height; r++) {
            int itemIdx = scrollOffset + r;
            boolean isSelected = (itemIdx == selectedIndex);
            int rowBg = isSelected ? 0x555555 : bgColor;
            
            String txt = (itemIdx >= 0 && itemIdx < items.size()) ? items.get(itemIdx) : "";
            for (int c = 0; c < width; c++) {
                char ch = (c < txt.length()) ? txt.charAt(c) : ' ';
                canvas.writeCell(x + c, y + r, ch, fgColor, rowBg);
            }
        }
    }

    @Override
    public boolean handleMouseClick(int cellX, int cellY, boolean isPressed) {
        if (contains(cellX, cellY) && isPressed) {
            int clickedRow = cellY - y;
            int itemIdx = scrollOffset + clickedRow;
            if (itemIdx >= 0 && itemIdx < items.size()) {
                selectedIndex = itemIdx;
            }
            return true;
        }
        return false;
    }
    
    public void scroll(int delta) {
        scrollOffset += delta;
        if (scrollOffset < 0) scrollOffset = 0;
        int maxScroll = Math.max(0, items.size() - height);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }
    
    public int getSelectedIndex() { return selectedIndex; }
    public String getSelectedItem() { 
        if (selectedIndex >= 0 && selectedIndex < items.size()) return items.get(selectedIndex);
        return null;
    }
}
