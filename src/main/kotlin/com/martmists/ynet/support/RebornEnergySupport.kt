package com.martmists.ynet.support

import com.martmists.ynet.transfer.handler.EnergyTransferHandler
import com.martmists.ynet.transfer.type.EnergyType
import com.martmists.ynet.util.YNetRegistry

object RebornEnergySupport : Runnable {
    override fun run() {
        YNetRegistry.register(EnergyType, EnergyTransferHandler)
    }
}