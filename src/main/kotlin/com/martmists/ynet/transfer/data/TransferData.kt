package com.martmists.ynet.transfer.data

import com.martmists.ynet.transfer.type.Type
import net.minecraft.item.ItemStack

interface TransferData<T: Type> {
    fun isEmpty(): Boolean
}
