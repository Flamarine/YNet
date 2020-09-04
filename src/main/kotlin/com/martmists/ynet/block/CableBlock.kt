package com.martmists.ynet.block

import com.martmists.ynet.YNetMod
import com.martmists.ynet.network.Network
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ConnectingBlock
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess

class CableBlock(settings: Settings) : ConnectingBlock(0.1875f, settings) {
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return withConnectionProperties(ctx.world, ctx.blockPos)
    }

    override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, fromPos: BlockPos, notify: Boolean) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify)
        val x = withConnectionProperties(world, pos)
        if (state != x) {
            world.setBlockState(pos, x)
        }
    }

    override fun getStateForNeighborUpdate(state: BlockState, facing: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
        return if (!state.canPlaceAt(world, pos)) {
            world.blockTickScheduler.schedule(pos, this, 1)
            super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos)
        } else {
            state.with(FACING_PROPERTIES[facing], YNetMod.shouldConnectCable(world as World, pos))
        }
    }

    override fun onBroken(world: WorldAccess, pos: BlockPos, state: BlockState) {
        Network.removeCable(pos)
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        val tStart = System.nanoTime()
        Network.addCable(pos)
        val tEnd = System.nanoTime()
        println((tEnd - tStart).toString() + "ns")
    }

    fun withConnectionProperties(world: BlockView, pos: BlockPos): BlockState {
        return defaultState
                .with(DOWN, YNetMod.shouldConnectCable(world as World, pos.down()))
                .with(UP, YNetMod.shouldConnectCable(world, pos.up()))
                .with(NORTH, YNetMod.shouldConnectCable(world, pos.north()))
                .with(EAST, YNetMod.shouldConnectCable(world, pos.east()))
                .with(SOUTH, YNetMod.shouldConnectCable(world, pos.south()))
                .with(WEST, YNetMod.shouldConnectCable(world, pos.west()))
    }

    init {
        defaultState = getStateManager().defaultState
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(UP, false)
                .with(DOWN, false)
    }
}