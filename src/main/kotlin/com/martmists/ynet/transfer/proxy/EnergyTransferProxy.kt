package com.martmists.ynet.transfer.proxy

import com.martmists.ynet.network.InteractionMode
import com.martmists.ynet.transfer.data.EnergyTransferData
import com.martmists.ynet.transfer.type.EnergyType
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack

class EnergyTransferProxy(override val be: BlockEntity,
                          override val mode: InteractionMode,
                          override val priority: Int,
                          override val filter: List<ItemStack>) : TransferProxy<EnergyType, EnergyTransferData> {
    override fun canExtract(data: EnergyTransferData): Boolean {
        TODO("Not yet implemented")
    }

    override fun canInsert(data: EnergyTransferData): Boolean {
        TODO("Not yet implemented")
    }

    override fun extract(data: EnergyTransferData) {
        TODO("Not yet implemented")
    }

    override fun insert(data: EnergyTransferData) {
        TODO("Not yet implemented")
    }
}