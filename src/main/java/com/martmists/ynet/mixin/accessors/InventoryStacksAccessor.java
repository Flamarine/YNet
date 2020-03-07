package com.martmists.ynet.mixin.accessors;

import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LootableContainerBlockEntity.class)
public interface InventoryStacksAccessor {
    @Invoker("getInvStackList")
    DefaultedList<ItemStack> callGetInvStackList();
}
