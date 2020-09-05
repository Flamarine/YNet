package com.martmists.ynet.transfer.type

import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import team.reborn.energy.EnergyStorage

object EnergyType : Type {
    override val identifier = Identifier("ynet:energy")
    override val color = 0xFFFF00
    override val canFilter = false

    override fun appliesTo(world: World, pos: BlockPos) = world.getBlockState(pos).block is EnergyStorage

    override fun canFilter(stack: ItemStack) = false
}