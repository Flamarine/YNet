package com.martmists.ynet.support

import com.martmists.ynet.transfer.handler.ItemTransferHandler
import com.martmists.ynet.transfer.type.ItemType
import com.martmists.ynet.util.YNetRegistry

object VanillaSupport : Runnable {
    override fun run() {
        YNetRegistry.register(ItemType, ItemTransferHandler)
    }
}