package com.martmists.ynet.transfer.proxy

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.item.ItemAttributes
import alexiil.mc.lib.attributes.item.filter.ItemFilter
import com.martmists.ynet.network.InteractionMode
import com.martmists.ynet.transfer.data.ItemTransferData
import com.martmists.ynet.transfer.type.ItemType
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack

class ItemTransferProxy(override val be: BlockEntity,
                        override val mode: InteractionMode,
                        override val priority: Int,
                        override val filter: List<ItemStack>) : TransferProxy<ItemType, ItemTransferData> {
    val MAX_AMOUNT = 64
    var amountMoved = 0

    override fun canExtract(data: ItemTransferData): Boolean {
        val ex = ItemAttributes.EXTRACTABLE.getFirstOrNull(be.world, be.pos) ?: return false
        val stack = ex.attemptExtraction(this::filterMatches, MAX_AMOUNT - amountMoved, Simulation.SIMULATE)
        data.stack = stack
        return !stack.isEmpty
    }

    override fun canInsert(data: ItemTransferData): Boolean {
        val ins = ItemAttributes.INSERTABLE.getFirstOrNull(be.world, be.pos) ?: return false
        val stack = ins.filtered(this::filterMatches).attemptInsertion(data.stack, Simulation.SIMULATE)
        data.amount = data.stack.count - stack.count
        return data.amount != 0
    }

    override fun extract(data: ItemTransferData) {
        val ex = ItemAttributes.EXTRACTABLE.get(be.world, be.pos)
        val stack = ex.extract(data.stack, data.amount)
        amountMoved += stack.count
    }

    override fun insert(data: ItemTransferData) {
        val ins = ItemAttributes.INSERTABLE.get(be.world, be.pos)
        val stack = ins.insert(data.stack)
        data.stack = stack
    }

    private fun filterMatches(item: ItemStack): Boolean {
        return filter.isNotEmpty() && filter.any { it.item == item.item }
    }
}