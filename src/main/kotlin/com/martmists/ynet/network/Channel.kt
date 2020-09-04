package com.martmists.ynet.network

import com.martmists.ynet.transfer.type.DisabledType
import com.martmists.ynet.transfer.type.Type

class Channel<T : Type>(val network: Network, val connectedBlocks: MutableList<ConfiguredBlockEntity>, var type: T) {
    companion object {
        fun empty(network: Network) = Channel(network, mutableListOf(), DisabledType)
    }
}
