package com.martmists.ynet.mixin.accessors;

import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(LootableContainerBlockEntity.class)
public interface InventoryStacksAccessor {
    @Invoker
    List<ItemStack> callGetInvStacks();
}
