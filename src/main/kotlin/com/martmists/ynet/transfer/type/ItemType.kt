package com.martmists.ynet.transfer.type

import alexiil.mc.lib.attributes.item.ItemAttributes
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object ItemType : Type {
    override val identifier = Identifier("ynet:item")
    override val color = 0x00AA00
    override val canFilter = true

    override fun appliesTo(world: World, pos: BlockPos): Boolean {
        val canInsert = ItemAttributes.INSERTABLE.getFirstOrNull(world, pos) != null
        val canExtract = ItemAttributes.EXTRACTABLE.getFirstOrNull(world, pos) != null
        return canInsert || canExtract
    }

    override fun canFilter(stack: ItemStack) = true
}
