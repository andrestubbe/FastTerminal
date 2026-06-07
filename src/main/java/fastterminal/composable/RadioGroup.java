package fastterminal.composable;
import fastterminal.component.Component;
import fastterminal.component.Panel;

import java.util.ArrayList;
import java.util.List;

public class RadioGroup {
    private List<RadioButton> buttons = new ArrayList<>();
    
    public void add(RadioButton btn) {
        buttons.add(btn);
    }
    
    public void select(RadioButton btn) {
        for (RadioButton b : buttons) {
            b.setSelected(b == btn);
        }
    }
}
