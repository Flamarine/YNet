package com.martmists.ynet.block

import com.martmists.ynet.YNetMod
import com.martmists.ynet.blockentity.ControllerBlockEntity
import com.martmists.ynet.network.Network
import com.martmists.ynet.screen.ControllerConfigScreenHandler
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.FacingBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.state.StateManager
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

class ControllerBlock(settings: Settings?) : FacingBlock(settings), BlockEntityProvider {
    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(FACING, ctx.playerLookDirection)
    }

    override fun createBlockEntity(view: BlockView): BlockEntity? {
        return ControllerBlockEntity()
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (!world.isClient) {
            player.openHandledScreen(object : ExtendedScreenHandlerFactory {
                override fun writeScreenOpeningData(player: ServerPlayerEntity, buffer: PacketByteBuf) {
                    buffer.writeBlockPos(pos)
                }

                override fun getDisplayName(): Text {
                    return TranslatableText(translationKey)
                }

                override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity): ScreenHandler {
                    return ControllerConfigScreenHandler(syncId, player, pos)
                }
            })
        }
        return ActionResult.SUCCESS
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
        super.onBreak(world, pos, state, player)
        Network.networks.remove((world.getBlockEntity(pos) as ControllerBlockEntity).network)
    }

    init {
        defaultState = stateManager.defaultState.with(FACING, Direction.NORTH)
    }
}
