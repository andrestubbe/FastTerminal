package fastterminal.composable;
import fastterminal.component.Component;
import fastterminal.component.Panel;

import fastterminal.FastTerminalScene;

public class TextBox extends Component {
    private StringBuilder text = new StringBuilder();
    private boolean focused = false;
    private int cursorPosition = 0;

    public TextBox(int x, int y, int width) {
        super(x, y, width, 1);
        this.bgColor = 0x222222;
        this.fgColor = 0xFFFFFF;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible) return;
        int currentBg = focused ? 0x444444 : bgColor;
        
        for (int i = 0; i < width; i++) {
            char ch = ' ';
            if (i < text.length()) {
                ch = text.charAt(i);
            }
            
            int cellBg = currentBg;
            if (focused && i == cursorPosition) {
                cellBg = 0xAAAAAA; // Cursor block
            }
            canvas.writeCell(x + i, y, ch, fgColor, cellBg);
        }
    }

    @Override
    protected void onPress() {
        // Assume external focus manager will unfocus others
        focused = true;
    }

    public void setFocused(boolean focused) { this.focused = focused; }
    public boolean isFocused() { return focused; }
    
    public void handleKey(int vKey, char keyChar) {
        if (!focused) return;
        
        if (vKey == 0x08) { // Backspace
            if (text.length() > 0 && cursorPosition > 0) {
                text.deleteCharAt(cursorPosition - 1);
                cursorPosition--;
            }
        } else if (vKey == 0x25) { // Left
            if (cursorPosition > 0) cursorPosition--;
        } else if (vKey == 0x27) { // Right
            if (cursorPosition < text.length()) cursorPosition++;
        } else if (Character.isDefined(keyChar) && keyChar >= 32 && keyChar < 127) {
            if (text.length() < width - 1) { // Leave room for cursor
                text.insert(cursorPosition, keyChar);
                cursorPosition++;
            }
        }
    }

    public String getText() { return text.toString(); }
}
