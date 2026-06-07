package fastterminal.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MouseDispatcher {
    
    private List<Component> components = new ArrayList<>();
    private Component draggingComponent = null;
    
    public void register(Component component) {
        components.add(component);
    }
    
    public void unregister(Component component) {
        components.remove(component);
        if (draggingComponent == component) {
            draggingComponent = null;
        }
    }
    
    /**
     * Call this inside your FastMouseListener.onMouseMove.
     * @return true if a component consumed the event
     */
    public boolean onMouseMove(int absX, int absY) {
        if (draggingComponent != null) {
            draggingComponent.handleMouseDrag(absX, absY);
            return true;
        }
        
        boolean consumed = false;
        // Iterate backwards (top-most component first assuming later additions are drawn on top)
        for (int i = components.size() - 1; i >= 0; i--) {
            Component c = components.get(i);
            if (!c.isVisible()) continue;
            
            c.handleMouseMove(absX, absY);
            if (!consumed && c.contains(absX, absY)) {
                consumed = true;
            }
        }
        return consumed;
    }
    
    /**
     * Call this inside your FastMouseListener.onMouseButton.
     * @return true if a component consumed the click
     */
    public boolean onMouseButton(int absX, int absY, boolean isPressed) {
        if (!isPressed && draggingComponent != null) {
            draggingComponent.handleMouseClick(absX, absY, false);
            draggingComponent = null;
            return true;
        }
        
        for (int i = components.size() - 1; i >= 0; i--) {
            Component c = components.get(i);
            if (!c.isVisible()) continue;
            
            if (c.contains(absX, absY)) {
                boolean handled = c.handleMouseClick(absX, absY, isPressed);
                if (handled) {
                    if (isPressed) {
                        draggingComponent = c;
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
