package com.martmists.ynet.transfer.type

import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object DisabledType : Type {
    override val identifier = Identifier("ynet:disabled")
    override val color = 0x000000
    override val canFilter = false

    override fun appliesTo(world: World, pos: BlockPos) = false
    override fun canFilter(stack: ItemStack): Boolean = false
}
