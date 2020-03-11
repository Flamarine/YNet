package spinnery.widget;

import net.minecraft.item.ItemStack;
import spinnery.client.BaseRenderer;

public class WItem extends WAbstractWidget {
    private ItemStack itemStack;

    @Override
    public void draw() {
        BaseRenderer.getItemRenderer().renderGuiItem(itemStack, getX(), getY());
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
