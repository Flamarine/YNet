package spinnery.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import spinnery.common.BaseContainer;
import spinnery.common.BaseHandledScreen;

public class BaseContainerScreen<T extends BaseContainer> extends BaseHandledScreen<T> {
    public BaseContainerScreen(Text name, T linkedContainer, PlayerEntity player) {
        super(name, linkedContainer, player);
    }
}
