package com.martmists.ynet.transfer.type

import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface Type {
    val identifier: Identifier
    val color: Int
    val canFilter: Boolean

    fun appliesTo(world: World, pos: BlockPos): Boolean
    fun canFilter(stack: ItemStack): Boolean
}
