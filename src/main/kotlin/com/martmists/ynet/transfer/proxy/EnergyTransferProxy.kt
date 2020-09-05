package com.martmists.ynet.transfer.proxy

import com.martmists.ynet.network.InteractionMode
import com.martmists.ynet.transfer.data.EnergyTransferData
import com.martmists.ynet.transfer.type.EnergyType
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import kotlin.math.min

class EnergyTransferProxy(override val be: BlockEntity,
                          override val mode: InteractionMode,
                          override val priority: Int,
                          override val filter: List<ItemStack>) : TransferProxy<EnergyType, EnergyTransferData> {
    override fun canExtract(data: EnergyTransferData): Boolean {
        val max = (be as EnergyStorage).getMaxOutput(EnergySide.UNKNOWN)
        val stored = be.getStored(EnergySide.UNKNOWN)
        data.energy = min(max, stored)
        return max != 0.0 && stored != 0.0
    }

    override fun canInsert(data: EnergyTransferData): Boolean {
        val max = (be as EnergyStorage).getMaxInput(EnergySide.UNKNOWN)
        val stored = be.getStored(EnergySide.UNKNOWN)
        val free = be.maxStoredPower - stored
        data.energy = min(data.energy, min(max, free))
        return max != 0.0 && free != 0.0
    }

    override fun extract(data: EnergyTransferData) {
        (be as EnergyStorage).setStored(be.getStored(EnergySide.UNKNOWN) - data.energy)
    }

    override fun insert(data: EnergyTransferData) {
        (be as EnergyStorage).setStored(be.getStored(EnergySide.UNKNOWN) + data.energy)
    }
}
