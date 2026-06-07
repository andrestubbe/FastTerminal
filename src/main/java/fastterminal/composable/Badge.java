package fastterminal.composable;
import fastterminal.component.Component;
import fastterminal.component.Panel;

import fastterminal.FastTerminalScene;

public class Badge {
    private int fgColor;
    private int bgColor;

    public Badge(int fgColor, int bgColor) {
        this.fgColor = fgColor;
        this.bgColor = bgColor;
    }

    public void draw(FastTerminalScene s, int x, int y, String status) {
        String txt = " " + status + " ";
        s.writeString(x, y, txt, fgColor, bgColor);
    }
}
