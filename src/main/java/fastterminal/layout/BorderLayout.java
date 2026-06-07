package fastterminal.layout;
import fastterminal.component.Component;
import fastterminal.component.Panel;

import java.util.List;

public class BorderLayout implements Layout {
    public enum Region { NORTH, SOUTH, EAST, WEST, CENTER }
    
    private Component north, south, east, west, center;
    
    public void setNorth(Component c) { north = c; }
    public void setSouth(Component c) { south = c; }
    public void setEast(Component c) { east = c; }
    public void setWest(Component c) { west = c; }
    public void setCenter(Component c) { center = c; }

    @Override
    public void layout(int parentX, int parentY, int parentWidth, int parentHeight, List<Component> children) {
        int top = parentY;
        int bottom = parentY + parentHeight;
        int left = parentX;
        int right = parentX + parentWidth;
        
        if (north != null) {
            north.setX(left);
            north.setY(top);
            north.setWidth(parentWidth);
            top += north.getHeight();
        }
        if (south != null) {
            south.setX(left);
            south.setY(bottom - south.getHeight());
            south.setWidth(parentWidth);
            bottom -= south.getHeight();
        }
        if (west != null) {
            west.setX(left);
            west.setY(top);
            west.setHeight(bottom - top);
            left += west.getWidth();
        }
        if (east != null) {
            east.setX(right - east.getWidth());
            east.setY(top);
            east.setHeight(bottom - top);
            right -= east.getWidth();
        }
        if (center != null) {
            center.setX(left);
            center.setY(top);
            center.setWidth(right - left);
            center.setHeight(bottom - top);
        }
    }
}
