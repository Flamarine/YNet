package com.martmists.ynet.screen

import com.github.vini2003.blade.common.data.Color
import com.github.vini2003.blade.common.data.Position
import com.github.vini2003.blade.common.data.Size
import com.github.vini2003.blade.common.data.Slots
import com.github.vini2003.blade.common.handler.BaseScreenHandler
import com.github.vini2003.blade.common.widget.base.ButtonWidget
import com.github.vini2003.blade.common.widget.base.SlotListWidget
import com.github.vini2003.blade.common.widget.base.SlotWidget
import com.github.vini2003.blade.common.widget.base.TextWidget
import com.martmists.ynet.YNetMod
import com.martmists.ynet.blockentity.ControllerBlockEntity
import com.martmists.ynet.ext.next
import com.martmists.ynet.network.Channel
import com.martmists.ynet.network.ConfiguredBlockEntity
import com.martmists.ynet.network.InteractionMode
import com.martmists.ynet.transfer.type.DisabledType
import com.martmists.ynet.util.WidgetBuilder
import com.martmists.ynet.util.YNetRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos

class ControllerConfigScreenHandler(syncId: Int, player: PlayerEntity, pos: BlockPos) : BaseScreenHandler(YNetMod.CONTROLLER_SCREEN_HANDLER, syncId, player) {
    val be = player.world.getBlockEntity(pos) as ControllerBlockEntity
    var currentChannel: Channel<*>? = null
    var currentBlock: ConfiguredBlockEntity? = null
    var selectedButton: ButtonWidget? = null
    val filterInventory = SimpleInventory(
            ItemStack.EMPTY,
            ItemStack.EMPTY,
            ItemStack.EMPTY,
            ItemStack.EMPTY,
            ItemStack.EMPTY,
            ItemStack.EMPTY,
            ItemStack.EMPTY,
            ItemStack.EMPTY
    )

    override fun canUse(player: PlayerEntity): Boolean {
        return true
    }

    override fun initialize(width: Int, height: Int) {
        //
        //                MAIN SCREEN
        //
        // +----Panel left----+ +----Panel right----+
        // |   ++channels++   | |                   |
        // | +----Blocks----+ | |                   |
        // | | B--cfg-+-+-+ | | |                   |
        // | | B--cfg-+-+-+ | | |                   |
        // | | B--cfg-+-+-+ | | |                   |
        // | | B--cfg-+-+-+ | | +-------------------+
        // | | B--cfg-+-+-+ | | +-----Inventory-----+
        // | | B--cfg-+-+-+ | | |                   |
        // | +--------------+ | |                   |
        // +------------------+ +-------------------+
        //
        //              CHANNEL SCREEN
        //
        // +----Panel left----+ +----Panel right----+
        // |   ++channels++   | |  [Channel Type]   |
        // | +----Blocks----+ | |                   |
        // | | B--cfg-+-+-+ | | |                   |
        // | | B--cfg-+-+-+ | | |                   |
        // | | B--cfg-+-+-+ | | |                   |
        // | | B--cfg-+-+-+ | | +-------------------+
        // | | B--cfg-+-+-+ | | +-----Inventory-----+
        // | | B--cfg-+-+-+ | | |                   |
        // | +--------------+ | |                   |
        // +------------------+ +-------------------+
        //
        //               BLOCK SCREEN
        //
        // +----Panel left----+ +----Panel right----+
        // |   ++channels++   | |      [Mode]       |
        // | +----Blocks----+ | | Priority  ______  |
        // | | B--cfg-+-+-+ | | | +----Filters----+ |
        // | | B--cfg-+-+-+ | | | |               | |
        // | | B--cfg-+-+-+ | | | +---------------+ |
        // | | B--cfg-+-+-+ | | +-------------------+
        // | | B--cfg-+-+-+ | | +-----Inventory-----+
        // | | B--cfg-+-+-+ | | |                   |
        // | +--------------+ | |                   |
        // +------------------+ +-------------------+

        val main = WidgetBuilder.build(this) {
            size(Size.of(370, 192))
            position(Position.of(width/2 - root.size.width/2, height/2 - root.size.height/2))

            // EMPTY
            val rightPanelEmpty = panel {
                size(Size.of(165, 95))
                relativePosition(200, 5)
                hidden(true)
            }

            // CHANNEL
            var rpcText: TextWidget? = null
            var rpcButton: ButtonWidget? = null
            val rightPanelChannel = panel {
                size(rightPanelEmpty.size)
                position(rightPanelEmpty.position)
                hidden(true)
                rpcText = text(LiteralText("Nothing here")) {
                    relativePosition(40, 5)
                }
                text(TranslatableText("ynet.ui.type")) {
                    relativePosition(10, 25)
                }
                rpcButton = button ({ channelTypeButton ->
                    val types = YNetRegistry.getTypes().toMutableList().also { it.add(0, DisabledType) }
                    println(currentChannel)
                    val newChannel = Channel(currentChannel!!.network, currentChannel!!.connectedBlocks.map { ConfiguredBlockEntity(it.controller, it.be, 0, InteractionMode.DISABLED, mutableListOf()) }.toMutableList(), types.next(currentChannel!!.type))
                    currentChannel!!.network.channels[currentChannel!!.network.channels.indexOf(currentChannel)] = newChannel
                    currentChannel = newChannel
                    channelTypeButton.label = TranslatableText(newChannel.type.identifier.toString().replace("ynet:", "ynet.ui."))
                }) {
                    size(Size.of(90, 20))
                    relativePosition(55, 20)
                }
            }

            // BLOCK
            var rpbButton: ButtonWidget? = null
            var rpbSlots: MutableCollection<SlotWidget>? = null
            val rightPanelBlock = panel {
                size(rightPanelEmpty.size)
                position(rightPanelEmpty.position)
                hidden(false)
                text(TranslatableText("ynet.ui.block")) {
                    relativePosition(40, 5)
                }
                text(TranslatableText("ynet.ui.mode")) {
                    relativePosition(10, 25)
                }
                rpbButton = button ({
                    val modes = InteractionMode.values().toList()
                    currentBlock!!.mode = modes.next(currentBlock!!.mode)
                    it.label = TranslatableText("ynet.ui.mode." + currentBlock!!.mode.name.toLowerCase())
                    when (currentBlock!!.mode) {
                        InteractionMode.INSERT -> {
                            selectedButton!!.label = LiteralText("I")
                        }
                        InteractionMode.EXTRACT -> {
                            selectedButton!!.label = LiteralText("E")
                        }
                        else -> {}
                    }
                }) {
                    size(Size.of(90, 20))
                    relativePosition(55, 20)
                }
                text(TranslatableText("ynet.ui.priority")) {
                    relativePosition(10, 45)
                }
                // TODO: Priority field
                rpbSlots = Slots.addArray(Position.of(root, 9, 70), Size.of(18, 18), root, 0, 8, 1, filterInventory)
            }

            // LEFT PANEL
            panel {
                size(Size.of(193, 182))
                relativePosition(5, 5)


                for (x in 0 until 10) {
                    button(Color.of(DisabledType.color), {
                        currentChannel = be.network.channels[x]
                        rpcButton!!.label = TranslatableText(currentChannel!!.type.identifier.toString().replace("ynet:", "ynet.ui."))
                        rpcText!!.text = TranslatableText("ynet.ui.channel.index", x)
                        rightPanelEmpty.hidden = true
                        rightPanelChannel.hidden = false
                        rightPanelBlock.hidden = true
                    }) {
                        size(Size.of(14, 14))
                        relativePosition(40 + 15*x, 6)
                        root.label = LiteralText(x.toString())
                    }
                }

                list {
                    size(Size.of(180, 140))
                    relativePosition(3, 20)

                    be.network.getConnectedBlocks().forEachIndexed { index, block ->
                        panel {
                            size(Size.of(180, 30))
                            relativePosition(3, 32 * index)

                            icon(be.world!!.getBlockState(block.pos).block) {
                                size(Size.of(20, 20))
                                relativePosition(4, 4)
                            }

                            for (x in 0 until 10) {
                                button ({
                                    if (be.network.channels[x].type == DisabledType) return@button
                                    selectedButton = it
                                    currentBlock = be.network.channels[x].connectedBlocks.first { b -> b.be == block }

                                    rpbButton!!.label = TranslatableText("ynet.ui.mode." + currentBlock!!.mode.name.toLowerCase())
                                    // TODO: Priority
                                    for (i in 0 until filterInventory.size()) {
                                        filterInventory.setStack(i, ItemStack.EMPTY)
                                    }
                                    currentBlock!!.filter.forEachIndexed { index, itemStack ->
                                        filterInventory.setStack(index, itemStack)
                                    }

                                    rightPanelEmpty.hidden = true
                                    rightPanelChannel.hidden = true
                                    rightPanelBlock.hidden = false
                                    rpbSlots?.forEach { s -> s.hidden = be.network.channels[x].type.canFilter }
                                }) {
                                    size(Size.of(14, 14))
                                    relativePosition(20 + 15*x, 6)
                                    root.label = LiteralText(" ")
                                }
                            }
                        }
                    }
                }
            }

            text(TranslatableText("container.inventory")) {
                relativePosition(202, 101)
            }
        }

        Slots.addPlayerInventory(Position.of(main, 202, 110), Size.of(18, 18), main, getPlayer().inventory)

        addWidget(main)
    }

    override fun onSlotClick(slotNumber: Int, button: Int, actionType: SlotActionType?, playerEntity: PlayerEntity?): ItemStack {
        val cursor = playerEntity!!.inventory.cursorStack
        if (slotNumber < 0 || currentBlock == null) return cursor
        val copy = cursor.copy()
        when {
            (slots[slotNumber] ?: return cursor).inventory == filterInventory -> {
                if (actionType == SlotActionType.PICKUP) {
                    if (button == 0) { // Put
                        if (!copy.isEmpty) {  // TODO: Check if can be filtered
                            copy.count = 1
                            val old = filterInventory.getStack(slotNumber)
                            if (!old.isEmpty) {
                                currentBlock!!.filter.remove(old)
                            }
                            filterInventory.setStack(slotNumber, copy)
                            currentBlock!!.filter.add(copy)
                        }
                    } else if (button == 1) {  // Take
                        val old = filterInventory.getStack(slotNumber)
                        if (!old.isEmpty) {
                            currentBlock!!.filter.remove(old)
                        }
                        filterInventory.setStack(slotNumber, ItemStack.EMPTY)
                    }
                }
                return cursor
            }
            actionType == SlotActionType.QUICK_MOVE -> {
                // TODO: Check if can be filtered
                var index = 0;
                for (i in 0 until filterInventory.size()) {
                    if (filterInventory.getStack(i).isEmpty) {
                        index = i
                        break;
                    }
                    return cursor
                }
                copy.count = 1
                filterInventory.setStack(index, copy)
                currentBlock!!.filter.add(copy)
                return cursor
            }
            else -> {
                return super.onSlotClick(slotNumber, button, actionType, playerEntity)
            }
        }
    }
}