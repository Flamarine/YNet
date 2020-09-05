package com.martmists.ynet.transfer.data

import com.martmists.ynet.transfer.type.EnergyType

class EnergyTransferData : TransferData<EnergyType> {
    var energy: Double = 0.0

    override fun isEmpty(): Boolean = energy == 0.0
}