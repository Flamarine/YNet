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
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import java.util.*

class ConnectorBlock(settings: Settings?) : ConnectingBlock(0.1875f, settings) {
    companion object {
        private val NORTH_CABLE = BooleanProperty.of("north_cable")
        private val EAST_CABLE = BooleanProperty.of("east_cable")
        private val SOUTH_CABLE = BooleanProperty.of("south_cable")
        private val WEST_CABLE = BooleanProperty.of("west_cable")
        private val UP_CABLE = BooleanProperty.of("up_cable")
        private val DOWN_CABLE = BooleanProperty.of("down_cable")
        private val CABLE_FACING_PROPERTIES: MutableMap<Direction, BooleanProperty> = EnumMap(Direction::class.java)

        init {
            CABLE_FACING_PROPERTIES[Direction.NORTH] = NORTH_CABLE
            CABLE_FACING_PROPERTIES[Direction.EAST] = EAST_CABLE
            CABLE_FACING_PROPERTIES[Direction.SOUTH] = SOUTH_CABLE
            CABLE_FACING_PROPERTIES[Direction.WEST] = WEST_CABLE
            CABLE_FACING_PROPERTIES[Direction.UP] = UP_CABLE
            CABLE_FACING_PROPERTIES[Direction.DOWN] = DOWN_CABLE
        }
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, NORTH_CABLE, EAST_CABLE, SOUTH_CABLE, WEST_CABLE, UP_CABLE, DOWN_CABLE)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
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
            state.with(FACING_PROPERTIES[facing], YNetMod.shouldConnect(world as World, neighborPos))
                 .with(CABLE_FACING_PROPERTIES[facing], YNetMod.shouldConnectCable(world, neighborPos))
        }
    }

    fun withConnectionProperties(world: BlockView, pos: BlockPos): BlockState {
        return defaultState
                .with(DOWN, YNetMod.shouldConnect(world as World, pos.down()))
                .with(UP, YNetMod.shouldConnect(world, pos.up()))
                .with(NORTH, YNetMod.shouldConnect(world, pos.north()))
                .with(EAST, YNetMod.shouldConnect(world, pos.east()))
                .with(SOUTH, YNetMod.shouldConnect(world, pos.south()))
                .with(WEST, YNetMod.shouldConnect(world, pos.west()))
                .with(DOWN_CABLE, YNetMod.shouldConnectCable(world, pos.down()))
                .with(UP_CABLE, YNetMod.shouldConnectCable(world, pos.up()))
                .with(NORTH_CABLE, YNetMod.shouldConnectCable(world, pos.north()))
                .with(EAST_CABLE, YNetMod.shouldConnectCable(world, pos.east()))
                .with(SOUTH_CABLE, YNetMod.shouldConnectCable(world, pos.south()))
                .with(WEST_CABLE, YNetMod.shouldConnectCable(world, pos.west()))
    }

    override fun onBroken(world: WorldAccess, pos: BlockPos, state: BlockState) {
        Network.removeConnector(pos)
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        Network.addConnector(world, pos)
        val x = withConnectionProperties(world, pos)
        if (state != x) {
            world.setBlockState(pos, x)
        }
    }

    init {
        defaultState = getStateManager().defaultState
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(UP, false)
                .with(DOWN, false)
                .with(NORTH_CABLE, false)
                .with(EAST_CABLE, false)
                .with(SOUTH_CABLE, false)
                .with(WEST_CABLE, false)
                .with(UP_CABLE, false)
                .with(DOWN_CABLE, false)
    }
}