package fastterminal.composable;
import fastterminal.component.Component;
import fastterminal.component.Panel;

import fastterminal.FastTerminalScene;

public class Table extends Component {
    private String[] headers;
    private String[][] data;
    private int[] columnWidths;

    public Table(int x, int y, int width, int height, String[] headers, String[][] data, int[] columnWidths) {
        super(x, y, width, height);
        this.headers = headers;
        this.data = data;
        this.columnWidths = columnWidths;
        this.bgColor = 0x111111;
        this.fgColor = 0xCCCCCC;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible) return;
        
        int currentY = y;
        // Render Header
        renderRow(canvas, currentY, headers, 0x444444, 0xFFFFFF);
        currentY++;
        
        // Render Data
        for (int i = 0; i < data.length && currentY < y + height; i++) {
            int rowBg = (i % 2 == 0) ? 0x222222 : 0x1A1A1A;
            renderRow(canvas, currentY, data[i], rowBg, fgColor);
            currentY++;
        }
        
        // Fill rest with background
        for (; currentY < y + height; currentY++) {
            for (int cx = x; cx < x + width; cx++) {
                canvas.writeCell(cx, currentY, ' ', fgColor, bgColor);
            }
        }
    }
    
    private void renderRow(FastTerminalScene canvas, int rowY, String[] rowData, int bg, int fg) {
        int currentX = x;
        for (int i = 0; i < columnWidths.length; i++) {
            String text = (i < rowData.length && rowData[i] != null) ? rowData[i] : "";
            int w = columnWidths[i];
            
            for (int c = 0; c < w; c++) {
                char ch = (c < text.length()) ? text.charAt(c) : ' ';
                if (currentX + c < x + width) {
                    canvas.writeCell(currentX + c, rowY, ch, fg, bg);
                }
            }
            currentX += w;
            if (currentX < x + width) {
                canvas.writeCell(currentX, rowY, '│', fg, bg);
                currentX++;
            }
        }
        for (; currentX < x + width; currentX++) {
             canvas.writeCell(currentX, rowY, ' ', fg, bg);
        }
    }
}
