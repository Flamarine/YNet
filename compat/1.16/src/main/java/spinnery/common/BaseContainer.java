package spinnery.common;

import net.minecraft.entity.player.PlayerInventory;
import spinnery.common.BaseScreenHandler;

public class BaseContainer extends BaseScreenHandler {
    public BaseContainer(int synchronizationID, PlayerInventory linkedPlayerInventory) {
        super(synchronizationID, linkedPlayerInventory);
    }
}
