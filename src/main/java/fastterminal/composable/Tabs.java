package fastterminal.composable;

import fastterminal.FastTerminalScene;
import fastterminal.component.Component;
import fastterminal.component.Panel;
import java.util.ArrayList;
import java.util.List;

public class Tabs extends Component {
    private List<String> tabTitles = new ArrayList<>();
    private List<Panel> tabPanels = new ArrayList<>();
    private int selectedIndex = 0;
    private int activeTabBg = 0x666666;
    private int activeTabFg = 0xFFFFFF;
    private int inactiveTabBg = 0x222222;
    private int inactiveTabFg = 0xAAAAAA;

    public Tabs(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void setActiveTabBg(int color) { this.activeTabBg = color; }
    public void setActiveTabFg(int color) { this.activeTabFg = color; }
    public void setInactiveTabBg(int color) { this.inactiveTabBg = color; }
    public void setInactiveTabFg(int color) { this.inactiveTabFg = color; }
    
    public void addTab(String title, Panel panel) {
        tabTitles.add(title);
        panel.setX(this.x);
        panel.setY(this.y + 1); // Below tab header
        panel.setWidth(this.width);
        panel.setHeight(this.height - 1);
        tabPanels.add(panel);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        for (Panel p : tabPanels) p.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        for (Panel p : tabPanels) p.setY(y + 1);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        for (Panel p : tabPanels) p.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        for (Panel p : tabPanels) p.setHeight(height - 1);
    }

    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible) return;
        
        // Draw tab headers
        int currentX = x;
        int TAB_WIDTH = 14;
        for (int i = 0; i < tabTitles.size(); i++) {
            String title = tabTitles.get(i);
            int pad = TAB_WIDTH - title.length();
            int padLeft = pad / 2;
            int padRight = pad - padLeft;
            String paddedTitle = " ".repeat(padLeft) + title + " ".repeat(padRight);
            
            int bg = (i == selectedIndex) ? activeTabBg : inactiveTabBg;
            int fg = (i == selectedIndex) ? activeTabFg : inactiveTabFg;
            
            for (int c = 0; c < paddedTitle.length(); c++) {
                if (currentX + c < x + width) {
                    canvas.writeCell(currentX + c, y, paddedTitle.charAt(c), fg, bg);
                }
            }
            currentX += TAB_WIDTH; // No gap
        }
        
        // Render selected panel
        if (selectedIndex >= 0 && selectedIndex < tabPanels.size()) {
            tabPanels.get(selectedIndex).render(canvas);
        }
    }

    @Override
    public boolean handleMouseClick(int cellX, int cellY, boolean isPressed) {
        if (cellY == y && isPressed) { // Clicked on header row
            int currentX = x;
            int TAB_WIDTH = 14;
            for (int i = 0; i < tabTitles.size(); i++) {
                if (cellX >= currentX && cellX < currentX + TAB_WIDTH) {
                    selectedIndex = i;
                    return true;
                }
                currentX += TAB_WIDTH;
            }
        } else if (selectedIndex >= 0 && selectedIndex < tabPanels.size()) {
            return tabPanels.get(selectedIndex).handleMouseClick(cellX, cellY, isPressed);
        }
        return false;
    }

    @Override
    public void handleMouseDrag(int cellX, int cellY) {
        if (selectedIndex >= 0 && selectedIndex < tabPanels.size()) {
            tabPanels.get(selectedIndex).handleMouseDrag(cellX, cellY);
        }
    }
    
    @Override
    public void handleMouseMove(int cellX, int cellY) {
        if (selectedIndex >= 0 && selectedIndex < tabPanels.size()) {
            tabPanels.get(selectedIndex).handleMouseMove(cellX, cellY);
        }
    }
    
    public int getSelectedIndex() {
        return selectedIndex;
    }
}
