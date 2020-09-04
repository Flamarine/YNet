package com.martmists.ynet.transfer.data

import com.martmists.ynet.transfer.type.ItemType
import net.minecraft.item.ItemStack

class ItemTransferData : TransferData<ItemType> {
    var stack = ItemStack.EMPTY
    var amount = 0

    override fun isEmpty(): Boolean {
        return stack.isEmpty
    }
}
