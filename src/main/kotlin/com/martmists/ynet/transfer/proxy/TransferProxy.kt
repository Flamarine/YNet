package com.martmists.ynet.transfer.proxy

import com.martmists.ynet.network.InteractionMode
import com.martmists.ynet.transfer.data.TransferData
import com.martmists.ynet.transfer.type.Type
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack

interface TransferProxy<T: Type, O: TransferData<T>> {
    val be: BlockEntity
    val mode: InteractionMode
    val priority: Int
    val filter: List<ItemStack>

    fun canExtract(data: O): Boolean
    fun canInsert(data: O): Boolean
    fun extract(data: O)
    fun insert(data: O)
}
