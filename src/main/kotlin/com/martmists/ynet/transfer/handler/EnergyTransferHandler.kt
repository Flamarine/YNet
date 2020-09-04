package com.martmists.ynet.transfer.handler

import com.martmists.ynet.network.ConfiguredBlockEntity
import com.martmists.ynet.transfer.data.EnergyTransferData
import com.martmists.ynet.transfer.proxy.EnergyTransferProxy
import com.martmists.ynet.transfer.type.EnergyType

object EnergyTransferHandler : TransferHandler<EnergyType, EnergyTransferData, EnergyTransferProxy> {
    override fun getProxy(cbe: ConfiguredBlockEntity): EnergyTransferProxy {
        TODO("Not yet implemented")
    }

    override fun getData(): EnergyTransferData {
        TODO("Not yet implemented")
    }
}