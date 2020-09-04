package com.martmists.ynet.network

import com.martmists.ynet.blockentity.ControllerBlockEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack

class ConfiguredBlockEntity(val controller: ControllerBlockEntity,
                            val be: BlockEntity, var priority: Int,
                            var mode: InteractionMode,
                            val filter: MutableList<ItemStack>) {
}
