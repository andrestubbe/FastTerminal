package fastterminal.composable;
import fastterminal.component.Component;
import fastterminal.component.Panel;

import fastterminal.FastTerminalScene;

public class CheckBox extends Component {
    private String label;
    private boolean checked;
    private Runnable onChange;

    public CheckBox(int x, int y, String label, boolean checked, Runnable onChange) {
        super(x, y, label.length() + 4, 1);
        this.label = label;
        this.checked = checked;
        this.onChange = onChange;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible) return;
        String box = checked ? "[X]" : "[ ]";
        String txt = box + " " + label;
        
        int currentBg = isHovered ? 0x333333 : bgColor;
        for (int i = 0; i < width; i++) {
            char ch = (i < txt.length()) ? txt.charAt(i) : ' ';
            canvas.writeCell(x + i, y, ch, fgColor, currentBg);
        }
    }

    @Override
    protected void onPress() {
        checked = !checked;
        if (onChange != null) onChange.run();
    }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
    public void setLabel(String label) {
        this.label = label;
        this.width = label.length() + 4;
    }
}
