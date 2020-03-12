package com.martmists.ynet.containers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.PacketByteBuf;
import spinnery.common.BaseContainer;
import spinnery.widget.WInterface;
import spinnery.widget.WSlot;
import spinnery.widget.api.Action;

public class ControllerContainer extends BaseContainer {
    public PlayerInventory playerInv;
    public PacketByteBuf packet;

    public ControllerContainer(int synchronizationID, PlayerInventory linkedPlayerInventory, PacketByteBuf packet) {
        super(synchronizationID, linkedPlayerInventory);
        playerInv = linkedPlayerInventory;
        this.packet = packet;

        WInterface mainInterface = getInterface();
        WSlot.addHeadlessPlayerInventory(mainInterface);
    }

    @Override
    public void onSlotAction(int slotNumber, int inventoryNumber, int button, Action action, PlayerEntity player) {
        super.onSlotAction(slotNumber, inventoryNumber, button, action, player);
    }
}
