package fastterminal.layout;
import fastterminal.component.Component;
import fastterminal.component.Panel;

import java.util.List;

public class GridLayout implements Layout {
    private int rows;
    private int cols;
    private int hGap;
    private int vGap;

    public GridLayout(int rows, int cols, int hGap, int vGap) {
        this.rows = rows;
        this.cols = cols;
        this.hGap = hGap;
        this.vGap = vGap;
    }

    @Override
    public void layout(int parentX, int parentY, int parentWidth, int parentHeight, List<Component> children) {
        if (rows <= 0 || cols <= 0) return;
        int cellWidth = (parentWidth - (cols - 1) * hGap) / cols;
        int cellHeight = (parentHeight - (rows - 1) * vGap) / rows;
        
        for (int i = 0; i < children.size(); i++) {
            Component child = children.get(i);
            int r = i / cols;
            int c = i % cols;
            
            child.setX(parentX + c * (cellWidth + hGap));
            child.setY(parentY + r * (cellHeight + vGap));
            child.setWidth(cellWidth);
            child.setHeight(cellHeight);
        }
    }
}
