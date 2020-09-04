package com.martmists.ynet.network

import com.martmists.ynet.YNetMod
import com.martmists.ynet.blockentity.ControllerBlockEntity
import com.martmists.ynet.util.YNetRegistry
import com.martmists.ynet.transfer.type.DisabledType
import com.martmists.ynet.transfer.type.Type
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class Network(val be: ControllerBlockEntity) {
    val world: World
        get() = be.world!!
    var didInit = false
    val cables = mutableSetOf<Long>()  // BlockPos.asLong
    val connectors = mutableSetOf<Long>()
    val channels = arrayOf<Channel<*>>(
            Channel.empty(this),
            Channel.empty(this),
            Channel.empty(this),
            Channel.empty(this),
            Channel.empty(this),
            Channel.empty(this),
            Channel.empty(this),
            Channel.empty(this),
            Channel.empty(this),
            Channel.empty(this)
    )

    init {
        networks.add(this)
    }

    fun rescan() {
        cables.clear()
        connectors.clear()
        scan(be.pos)
        val connected = getConnectedBlocks()

        for ((index, channel) in channels.withIndex()) {
            val new = Channel(this, mutableListOf(), channel.type)
            new.connectedBlocks.addAll(connected.map { itt ->
                val old = channel.connectedBlocks.firstOrNull { it.be == itt }
                val cbe = ConfiguredBlockEntity(this.be, itt, old?.priority ?: 0, old?.mode ?: InteractionMode.DISABLED, old?.filter ?: mutableListOf())
                cbe
            })
            channels[index] = new
        }
        be.markDirty()
    }

    private fun getNeighbors(pos: BlockPos) = listOf(pos.up(), pos.down(), pos.north(), pos.east(), pos.south(), pos.west()).map { it.asLong() }

    private fun scan(origin: BlockPos) {
        val scanned = mutableSetOf(origin.asLong())
        val toScan = mutableSetOf(*getNeighbors(origin).toTypedArray())
        while (toScan.isNotEmpty()) {
            val next = toScan.elementAt(0)
            toScan.remove(next)

            for (side in getNeighbors(BlockPos.fromLong(next))) {
                if (side !in scanned) {
                    scanned.add(side)

                    val b = world.getBlockState(BlockPos.fromLong(side)).block
                    if (b === YNetMod.CABLE) {
                        cables.add(side)
                    } else if (b === YNetMod.CONNECTOR) {
                        connectors.add(side)
                    } else {
                        continue
                    }
                    toScan.add(side)
                }
            }
        }
    }

    fun tick() {
        if (!didInit) {
            rescan()
            didInit = true
        }

        if (world.isClient) return

        channels.forEach {
            if (it.type != DisabledType) {
                println("ticking channel $it of type ${it.type}")
                YNetRegistry.getHandler(it.type)?.tick(it as Channel<Type>)
            }
        }
    }

    fun getConnectedBlocks(): List<BlockEntity> {
        if (connectors.isEmpty()) return listOf()

        val allBlocks = mutableSetOf<Long>()

        for (c in connectors) {
            val pos = BlockPos.fromLong(c)
            for (side in getNeighbors(pos)) {
                if (YNetMod.shouldConnect(world, BlockPos.fromLong(side))) {
                    allBlocks.add(side)
                }
            }
        }

        return allBlocks.mapNotNull {
            world.getBlockEntity(BlockPos.fromLong(it))
        }.toList()
    }

    companion object {
        val networks = mutableListOf<Network>()

        fun addConnector(world: World, pos: BlockPos) {
            for (side in listOf(pos.up(), pos.down(), pos.north(), pos.east(), pos.south(), pos.west())) {
                if (world.getBlockState(side).block === YNetMod.CONTROLLER) {
                    val be = (world.getBlockEntity(side) as ControllerBlockEntity)
                    be.network.connectors.add(pos.asLong())
                    be.network.rescan()
                }

                for (net in networks) {
                    if (side.asLong() in net.connectors || side.asLong() in net.cables) {
                        net.connectors.add(pos.asLong())
                        net.rescan()
                    }
                }
            }
        }

        fun removeConnector(pos: BlockPos) {
            for (net in networks) {
                if (pos.asLong() in net.connectors) {
                    net.rescan()
                }
            }
        }

        fun addCable(pos: BlockPos) {
            for (side in listOf(pos.up(), pos.down(), pos.north(), pos.east(), pos.south(), pos.west())) {
                for (net in networks) {
                    if (side.asLong() in net.connectors || side.asLong() in net.cables) {
                        net.cables.add(pos.asLong())
                        net.rescan()
                    }
                }
            }
        }

        fun removeCable(pos: BlockPos) {
            for (net in networks) {
                if (pos.asLong() in net.cables) {
                    net.rescan()
                }
            }
        }
    }
}