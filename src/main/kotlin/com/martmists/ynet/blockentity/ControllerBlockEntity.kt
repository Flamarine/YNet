package com.martmists.ynet.blockentity

import com.martmists.ynet.YNetMod
import com.martmists.ynet.util.YNetRegistry
import com.martmists.ynet.network.Channel
import com.martmists.ynet.network.ConfiguredBlockEntity
import com.martmists.ynet.network.InteractionMode
import com.martmists.ynet.network.Network
import com.martmists.ynet.transfer.type.DisabledType
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.util.Identifier
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos

class ControllerBlockEntity : BlockEntity(YNetMod.CONTROLLER_BE), Tickable {
    val network by lazy {
        Network(this)
    }

    override fun tick() {
        network.tick()
    }

    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)
        val data = tag.getCompound("controllerData")
        val channels = data.getList("channels", NbtType.COMPOUND)
        channels.forEachIndexed { index, tag ->
            tag as CompoundTag
            val id = tag.getString("type")
            val type = YNetRegistry.getType(Identifier(id))
            val blocks = tag.getList("blocks", NbtType.COMPOUND)

            val channel = Channel(this.network, mutableListOf(), type ?: DisabledType)

            if (channel.type == DisabledType) return@forEachIndexed;

            for (block in blocks) {
                block as CompoundTag
                val priority = block.getInt("priority")
                val mode = block.getString("mode")
                val pos = block.getLong("pos")
                val filters = block.getList("item", NbtType.COMPOUND)
                val filterItems = filters.map { ItemStack.fromTag(it as CompoundTag) }.toMutableList()
                val be = world!!.getBlockEntity(BlockPos.fromLong(pos))
                val cbe = ConfiguredBlockEntity(this, be!!, priority, InteractionMode.valueOf(mode), filterItems)
                channel.connectedBlocks.add(cbe)
            }

            network.channels[index] = channel;
        }
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        val data = CompoundTag()
        val channels = ListTag()
        network.channels.forEachIndexed { index, channel ->
            val ctag = CompoundTag()
            ctag.putString("type", channel.type.identifier.toString())
            val blocks = ListTag()
            if (channel.type != DisabledType) {
                channel.connectedBlocks.forEach { cbe ->
                    val block = CompoundTag()
                    block.putInt("priority", cbe.priority)
                    block.putString("mode", cbe.mode.name)
                    block.putLong("pos", cbe.be.pos.asLong())
                    val filters = ListTag()
                    cbe.filter.forEach {
                        filters.add(it.toTag(CompoundTag()))
                    }
                    block.put("item", filters)
                    blocks.add(block)
                }
            }
            ctag.put("blocks", blocks)
            channels.add(ctag)
        }
        data.put("channels", channels)
        tag.put("controllerData", data)
        return tag
    }
}