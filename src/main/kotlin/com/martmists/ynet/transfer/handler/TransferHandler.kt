package com.martmists.ynet.transfer.handler

import com.martmists.ynet.network.Channel
import com.martmists.ynet.network.ConfiguredBlockEntity
import com.martmists.ynet.network.InteractionMode
import com.martmists.ynet.transfer.data.TransferData
import com.martmists.ynet.transfer.proxy.ItemTransferProxy
import com.martmists.ynet.transfer.proxy.TransferProxy
import com.martmists.ynet.transfer.type.Type

interface TransferHandler<T: Type, D: TransferData<T>, O: TransferProxy<T, D>> {
    fun tick(channel: Channel<T>) {
        val blocks = channel.connectedBlocks.map(::getProxy).toList()
        val data = getData()

        val extract = blocks.filter { it.mode == InteractionMode.EXTRACT }.sortedByDescending { it.priority }.toMutableList()
        println("Extracting from: $extract")
        val insert = blocks.filter { it.mode == InteractionMode.INSERT }.toMutableList()
        println("Inserting into: $insert")

        for (ex in extract) {
            if (!ex.canExtract(data)) {
                continue
            }

            for (ins in insert) {
                if (ins.canInsert(data) && !data.isEmpty()) {
                    ex.extract(data)
                    ins.insert(data)
                }
            }
        }
    }

    fun getProxy(cbe: ConfiguredBlockEntity): O
    fun getData(): D
}
