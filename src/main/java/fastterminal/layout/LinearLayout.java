package fastterminal.layout;
import fastterminal.component.Component;
import fastterminal.component.Panel;

import java.util.List;

public class LinearLayout implements Layout {
    public enum Direction { HORIZONTAL, VERTICAL }
    
    private Direction direction;
    private int spacing;

    public LinearLayout(Direction direction, int spacing) {
        this.direction = direction;
        this.spacing = spacing;
    }

    @Override
    public void layout(int parentX, int parentY, int parentWidth, int parentHeight, List<Component> children) {
        int currentX = parentX;
        int currentY = parentY;
        
        for (Component child : children) {
            child.setX(currentX);
            child.setY(currentY);
            
            if (direction == Direction.HORIZONTAL) {
                currentX += child.getWidth() + spacing;
            } else {
                currentY += child.getHeight() + spacing;
            }
        }
    }
}
