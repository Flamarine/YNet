package com.martmists.ynet.screen

import com.github.vini2003.blade.common.data.Color
import com.github.vini2003.blade.common.data.Position
import com.github.vini2003.blade.common.data.Size
import com.github.vini2003.blade.common.data.Slots
import com.github.vini2003.blade.common.handler.BaseScreenHandler
import com.github.vini2003.blade.common.widget.base.*
import com.martmists.ynet.YNetMod
import com.martmists.ynet.blockentity.ControllerBlockEntity
import com.martmists.ynet.ext.next
import com.martmists.ynet.ext.ofRGB
import com.martmists.ynet.network.Channel
import com.martmists.ynet.network.ConfiguredBlockEntity
import com.martmists.ynet.network.InteractionMode
import com.martmists.ynet.transfer.type.DisabledType
import com.martmists.ynet.screen.util.ColoredButtonWidget
import com.martmists.ynet.screen.util.WidgetBuilder
import com.martmists.ynet.util.YNetRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos

class ControllerConfigScreenHandler(syncId: Int, player: PlayerEntity, pos: BlockPos) : BaseScreenHandler(YNetMod.CONTROLLER_SCREEN_HANDLER, syncId, player) {
    val be = player.world.getBlockEntity(pos) as ControllerBlockEntity
    var currentBlockSet = false

    var channelIndex = -1
    val currentChannel: Channel<*>
        get() = be.network.channels[channelIndex]
    lateinit var currentBlock: ConfiguredBlockEntity
    lateinit var selectedButton: AbstractWidget

    lateinit var rpcText: TextWidget
    lateinit var rpcButton: ButtonWidget
    lateinit var rpbButton: ButtonWidget
    lateinit var rpbSlots: MutableCollection<SlotWidget>

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
            size(Size.of(390, 192))
            position(Position.of(width/2 - root.size.width/2, height/2 - root.size.height/2))

            // EMPTY
            val rightPanelEmpty = panel {
                size(Size.of(165, 95))
                relativePosition(220, 5)
                hidden(false)
            }

            // CHANNEL
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
                    val newChannel = Channel(currentChannel.network, currentChannel.connectedBlocks.map { ConfiguredBlockEntity(it.controller, it.be, 0, InteractionMode.DISABLED, mutableListOf()) }.toMutableList(), types.next(currentChannel.type))
                    be.network.channels[channelIndex] = newChannel
                    channelTypeButton.label = TranslatableText(newChannel.type.identifier.toString().replace("ynet:", "ynet.ui.type."))
                    (selectedButton as ColoredButtonWidget).color = Color.ofRGB(newChannel.type.color)
                    be.markDirty()
                }) {
                    size(Size.of(90, 20))
                    relativePosition(55, 20)
                }
            }

            // BLOCK
            val rightPanelBlock = panel {
                size(rightPanelEmpty.size)
                position(rightPanelEmpty.position)
                hidden(true)
                text(TranslatableText("ynet.ui.block")) {
                    relativePosition(40, 5)
                }
                text(TranslatableText("ynet.ui.mode")) {
                    relativePosition(10, 25)
                }
                rpbButton = button ({
                    val modes = InteractionMode.values().toList()
                    currentBlock.mode = modes.next(currentBlock.mode)
                    it.label = TranslatableText("ynet.ui.mode." + currentBlock.mode.name.toLowerCase())
                    when (currentBlock.mode) {
                        InteractionMode.INSERT -> {
                            (selectedButton as ButtonWidget).label = LiteralText("I")
                        }
                        InteractionMode.EXTRACT -> {
                            (selectedButton as ButtonWidget).label = LiteralText("E")
                        }
                        else -> {}
                    }
                    be.markDirty()
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
                size(Size.of(213, 182))
                relativePosition(5, 5)


                for (x in 0 until 10) {
                    button(Color.ofRGB(be.network.channels[x].type.color), {
                        channelIndex = x
                        rpcButton.label = TranslatableText(currentChannel.type.identifier.toString().replace("ynet:", "ynet.ui.type."))
                        rpcText.text = TranslatableText("ynet.ui.channel.index", x)
                        rightPanelEmpty.hidden = true
                        rightPanelChannel.hidden = false
                        rightPanelBlock.hidden = true
                        selectedButton = it
                    }) {
                        size(Size.of(14, 14))
                        relativePosition(26 + 15*x, 6)
                        root.label = LiteralText(x.toString())
                    }
                }

                list {
                    size(Size.of(202, 140))
                    relativePosition(3, 25)

                    be.network.getConnectedBlocks().forEachIndexed { index, block ->
                        panel {
                            size(Size.of(175, 25))
                            relativePosition(3, 32 * index)

                            icon(be.world!!.getBlockState(block.pos).block, LiteralText(block.pos.toShortString())) {
                                size(Size.of(20, 20))
                                relativePosition(4, 5)

                            }

                            for (x in 0 until 10) {
                                button ({
                                    if (be.network.channels[x].type == DisabledType) return@button
                                    selectedButton = it
                                    currentBlock = be.network.channels[x].connectedBlocks.first { b -> b.be === block }
                                    currentBlockSet = true

                                    rpbButton.label = TranslatableText("ynet.ui.mode." + currentBlock.mode.name.toLowerCase())
                                    // TODO: Priority
                                    for (i in 0 until filterInventory.size()) {
                                        filterInventory.setStack(i, ItemStack.EMPTY)
                                    }
                                    currentBlock.filter.forEachIndexed { index, itemStack ->
                                        filterInventory.setStack(index, itemStack)
                                    }

                                    rightPanelEmpty.hidden = true
                                    rightPanelChannel.hidden = true
                                    rightPanelBlock.hidden = false
                                    rpbSlots.forEach { s -> s.hidden = be.network.channels[x].type.canFilter }
                                }) {
                                    size(Size.of(14, 14))
                                    relativePosition(20 + 15*x, 6)
                                    val cbe = be.network.channels[x].connectedBlocks.first { b -> b.be == block }
                                    when (cbe.mode) {
                                        InteractionMode.INSERT -> {
                                            root.label = LiteralText("I")
                                        }
                                        InteractionMode.EXTRACT -> {
                                            root.label = LiteralText("E")
                                        }
                                        else -> {
                                            root.label = LiteralText(" ")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            text(TranslatableText("container.inventory")) {
                relativePosition(222, 101)
            }
        }

        Slots.addPlayerInventory(Position.of(main, 222, 110), Size.of(18, 18), main, getPlayer().inventory)

        addWidget(main)
    }

    override fun onSlotClick(slotNumber: Int, button: Int, actionType: SlotActionType?, playerEntity: PlayerEntity?): ItemStack {
        val cursor = playerEntity!!.inventory.cursorStack
        if (slotNumber < 0 || !currentBlockSet) return cursor
        val copy = cursor.copy()
        when {
            (slots[slotNumber] ?: return cursor).inventory == filterInventory -> {
                if (actionType == SlotActionType.PICKUP) {
                    if (button == 0) { // Put
                        if (!copy.isEmpty) {  // TODO: Check if can be filtered
                            copy.count = 1
                            val old = filterInventory.getStack(slotNumber)
                            if (!old.isEmpty) {
                                currentBlock.filter.remove(old)
                            }
                            filterInventory.setStack(slotNumber, copy)
                            currentBlock.filter.add(copy)
                        }
                    } else if (button == 1) {  // Take
                        val old = filterInventory.getStack(slotNumber)
                        if (!old.isEmpty) {
                            currentBlock.filter.remove(old)
                        }
                        filterInventory.setStack(slotNumber, ItemStack.EMPTY)
                    }
                    be.markDirty()
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
                currentBlock.filter.add(copy)
                be.markDirty()
                return cursor
            }
            else -> {
                return super.onSlotClick(slotNumber, button, actionType, playerEntity)
            }
        }
    }
}