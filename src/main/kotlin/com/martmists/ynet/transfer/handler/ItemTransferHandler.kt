package com.martmists.ynet.transfer.handler

import com.martmists.ynet.network.Channel
import com.martmists.ynet.network.ConfiguredBlockEntity
import com.martmists.ynet.network.InteractionMode
import com.martmists.ynet.transfer.data.ItemTransferData
import com.martmists.ynet.transfer.proxy.ItemTransferProxy
import com.martmists.ynet.transfer.proxy.TransferProxy
import com.martmists.ynet.transfer.type.ItemType

object ItemTransferHandler : TransferHandler<ItemType, ItemTransferData, ItemTransferProxy> {
    override fun getProxy(cbe: ConfiguredBlockEntity): ItemTransferProxy {
        return ItemTransferProxy(cbe.be, cbe.mode, cbe.priority, cbe.filter)
    }

    override fun getData(): ItemTransferData {
        return ItemTransferData()
    }
}