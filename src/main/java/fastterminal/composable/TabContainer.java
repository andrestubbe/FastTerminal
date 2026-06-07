package fastterminal.composable;

import fastterminal.FastTerminalScene;
import fastterminal.component.Component;
import java.util.ArrayList;
import java.util.List;

public class TabContainer extends Component {
    
    public static class Tab {
        public String icon;
        public Component content;
        
        public Tab(String icon, Component content) {
            this.icon = icon;
            this.content = content;
        }
    }
    
    private List<Tab> tabs = new ArrayList<>();
    private int activeTabIndex = 0;
    private int headerBgColor = 0xFFFF00;
    private int activeHeaderBgColor = 0xFFFFFF;
    private int headerFgColor = 0x000000;
    
    public TabContainer(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    
    public void addTab(String icon, Component content) {
        tabs.add(new Tab(icon, content));
        syncContentBounds();
    }
    
    public void setActiveTab(int index) {
        if (index >= 0 && index < tabs.size()) {
            activeTabIndex = index;
        }
    }
    
    public int getActiveTabIndex() {
        return activeTabIndex;
    }
    
    private void syncContentBounds() {
        for (Tab t : tabs) {
            if (t.content != null) {
                t.content.setX(x);
                t.content.setY(y + 1); // Content is below tab header
                t.content.setWidth(width);
                t.content.setHeight(height - 1);
            }
        }
    }
    
    @Override
    public void setX(int x) {
        super.setX(x);
        syncContentBounds();
    }
    
    @Override
    public void setY(int y) {
        super.setY(y);
        syncContentBounds();
    }
    
    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        syncContentBounds();
    }
    
    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        syncContentBounds();
    }
    
    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible || tabs.isEmpty()) return;
        
        // Render headers
        int cx = x;
        for (int i = 0; i < tabs.size(); i++) {
            Tab t = tabs.get(i);
            int bg = (i == activeTabIndex) ? activeHeaderBgColor : headerBgColor;
            String text = " " + t.icon + " ";
            canvas.writeString(cx, y, text, headerFgColor, bg);
            cx += text.length(); // Emoji counts as length! Wait, fastemojis uses length() effectively.
        }
        
        // Render active content
        Component activeContent = tabs.get(activeTabIndex).content;
        if (activeContent != null) {
            // Draw background for content area
            for (int r = 1; r < height; r++) {
                for (int c = 0; c < width; c++) {
                    canvas.writeCell(x + c, y + r, ' ', 0xFFFFFF, bgColor);
                }
            }
            activeContent.render(canvas);
        }
    }
    
    @Override
    public boolean handleMouseClick(int cellX, int cellY, boolean isPressed) {
        if (!visible || tabs.isEmpty()) return false;
        
        // Click on header
        if (cellY == y && cellX >= x && cellX < x + width) {
            if (isPressed) {
                int cx = x;
                for (int i = 0; i < tabs.size(); i++) {
                    Tab t = tabs.get(i);
                    int textLen = 2 + t.icon.length(); 
                    if (cellX >= cx && cellX < cx + textLen) {
                        setActiveTab(i);
                        return true;
                    }
                    cx += textLen;
                }
            }
            return true;
        }
        
        // Delegate to active tab
        Component activeContent = tabs.get(activeTabIndex).content;
        if (activeContent != null && activeContent.contains(cellX, cellY)) {
            return activeContent.handleMouseClick(cellX, cellY, isPressed);
        }
        return false;
    }
    
    @Override
    public void handleMouseDrag(int cellX, int cellY) {
        if (!visible || tabs.isEmpty()) return;
        Component activeContent = tabs.get(activeTabIndex).content;
        if (activeContent != null) {
            activeContent.handleMouseDrag(cellX, cellY);
        }
    }
    
    @Override
    public void handleMouseMove(int cellX, int cellY) {
        super.handleMouseMove(cellX, cellY);
        if (!visible || tabs.isEmpty()) return;
        Component activeContent = tabs.get(activeTabIndex).content;
        if (activeContent != null) {
            activeContent.handleMouseMove(cellX, cellY);
        }
    }
}
