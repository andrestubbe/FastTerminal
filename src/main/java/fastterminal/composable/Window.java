package fastterminal.composable;

import fastterminal.FastTerminalScene;
import fastterminal.component.Component;
import fastemojis.FastEmojis;

public class Window extends Component {
    private String title;
    private Component content;
    private boolean isExpanded = true;
    
    private boolean isDraggingWindow = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    
    private int titleBgColor = 0xFFA500;
    private int titleFgColor = 0x000000;
    
    public Window(int x, int y, int width, String title, Component content) {
        super(x, y, width, content != null ? content.getHeight() + 1 : 1);
        this.title = title;
        this.content = content;
        
        if (this.content != null) {
            this.content.setX(x);
            this.content.setY(y + 1);
            this.content.setWidth(width);
        }
    }
    
    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
        if (content != null) {
            content.setVisible(expanded);
        }
    }
    
    public boolean isExpanded() {
        return isExpanded;
    }
    
    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible) return;
        
        int currentTitleWidth = isExpanded ? width : Math.max(14, title.length() + 6);
        
        // Draw titlebar background
        for (int c = 0; c < currentTitleWidth; c++) {
            canvas.writeCell(x + c, y, ' ', titleFgColor, titleBgColor);
        }
        
        // Draw title
        canvas.writeString(x + 1, y, title, titleFgColor, titleBgColor);
        
        // Collapse/Expand button
        String toggleBtn = isExpanded ? "::" : "[]";
        canvas.writeString(x + currentTitleWidth - 3, y, toggleBtn, titleFgColor, titleBgColor);
        
        if (isExpanded && content != null) {
            content.render(canvas);
        }
    }
    
    @Override
    public boolean contains(int cellX, int cellY) {
        if (!visible) return false;
        int currentTitleWidth = isExpanded ? width : Math.max(14, title.length() + 6);
        
        if (cellY == y && cellX >= x && cellX < x + currentTitleWidth) return true;
        if (isExpanded && content != null) {
            return content.contains(cellX, cellY);
        }
        return false;
    }
    
    @Override
    public boolean handleMouseClick(int cellX, int cellY, boolean isPressed) {
        if (!visible) return false;
        
        if (!isPressed) {
            isDraggingWindow = false;
        }
        
        int currentTitleWidth = isExpanded ? width : Math.max(14, title.length() + 6);
        
        // Click on titlebar
        if (cellY == y && cellX >= x && cellX < x + currentTitleWidth) {
            if (isPressed) {
                if (cellX >= x + currentTitleWidth - 4 && cellX <= x + currentTitleWidth - 1) {
                    setExpanded(!isExpanded);
                } else {
                    isDraggingWindow = true;
                    dragOffsetX = cellX - x;
                    dragOffsetY = cellY - y;
                }
            }
            return true;
        }
        
        if (isExpanded && content != null && content.contains(cellX, cellY)) {
            return content.handleMouseClick(cellX, cellY, isPressed);
        }
        return false;
    }
    
    @Override
    public void handleMouseDrag(int cellX, int cellY) {
        if (isDraggingWindow) {
            setX(cellX - dragOffsetX);
            setY(cellY - dragOffsetY);
        } else if (isExpanded && content != null) {
            content.handleMouseDrag(cellX, cellY);
        }
    }
    
    @Override
    public void handleMouseMove(int cellX, int cellY) {
        super.handleMouseMove(cellX, cellY);
        if (isExpanded && content != null) {
            content.handleMouseMove(cellX, cellY);
        }
    }
    
    @Override
    public void setX(int x) {
        super.setX(x);
        if (content != null) content.setX(x);
    }
    
    @Override
    public void setY(int y) {
        super.setY(y);
        if (content != null) content.setY(y + 1);
    }
}
