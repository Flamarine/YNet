package spinnery.widget;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class WCustomTooltipItem extends WTooltipItem {

    @Override
    public void updateText() {
        // tooltipText.setText(stack.getName());
    }

    public void setText(String txt) {
        setText(new LiteralText(txt));
    }

    public void setText(Text txt) {
        this.tooltipText.setText(txt);
    }
}