package fastterminal.layout;
import fastterminal.component.Component;
import fastterminal.component.Panel;

import java.util.List;

public interface Layout {
    void layout(int parentX, int parentY, int parentWidth, int parentHeight, List<Component> children);
}
