package com.martmists.ynet.blocks;

import com.martmists.ynet.YNetMod;
import com.martmists.ynet.api.BaseProvider;
import com.martmists.ynet.blockentities.ConnectorBlockEntity;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import com.martmists.ynet.network.Network;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.ModifiableWorld;
import net.minecraft.world.World;

import java.util.*;

public class ConnectorBlock extends ConnectingBlock implements BlockEntityProvider {
    private static BooleanProperty NORTH_CABLE = BooleanProperty.of("north_cable");
    private static BooleanProperty EAST_CABLE = BooleanProperty.of("east_cable");
    private static BooleanProperty SOUTH_CABLE = BooleanProperty.of("south_cable");
    private static BooleanProperty WEST_CABLE = BooleanProperty.of("west_cable");
    private static BooleanProperty UP_CABLE = BooleanProperty.of("up_cable");
    private static BooleanProperty DOWN_CABLE = BooleanProperty.of("down_cable");
    private static Map<Direction, BooleanProperty> CABLE_FACING_PROPERTIES = new HashMap<>();

    static {
        CABLE_FACING_PROPERTIES.put(Direction.NORTH, NORTH_CABLE);
        CABLE_FACING_PROPERTIES.put(Direction.EAST, EAST_CABLE);
        CABLE_FACING_PROPERTIES.put(Direction.SOUTH, SOUTH_CABLE);
        CABLE_FACING_PROPERTIES.put(Direction.WEST, WEST_CABLE);
        CABLE_FACING_PROPERTIES.put(Direction.UP, UP_CABLE);
        CABLE_FACING_PROPERTIES.put(Direction.DOWN, DOWN_CABLE);
    }

    public ConnectorBlock(Settings settings) {
        super(0.1875F, settings);
        setDefaultState(getStateManager().getDefaultState()
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
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, NORTH_CABLE, EAST_CABLE, SOUTH_CABLE, WEST_CABLE, UP_CABLE, DOWN_CABLE);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.withConnectionProperties(ctx.getWorld(), ctx.getBlockPos());
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) {
            world.getBlockTickScheduler().schedule(pos, this, 1);
            return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
        } else {
            Block block = neighborState.getBlock();
            return state.with(FACING_PROPERTIES.get(facing), block == this || block == YNetMod.CONTROLLER || block instanceof BaseProvider)
                        .with(CABLE_FACING_PROPERTIES.get(facing), block == YNetMod.CABLE);
        }
    }

    public BlockState withConnectionProperties(BlockView world, BlockPos pos) {
        Block block = world.getBlockState(pos.down()).getBlock();
        Block block2 = world.getBlockState(pos.up()).getBlock();
        Block block3 = world.getBlockState(pos.north()).getBlock();
        Block block4 = world.getBlockState(pos.east()).getBlock();
        Block block5 = world.getBlockState(pos.south()).getBlock();
        Block block6 = world.getBlockState(pos.west()).getBlock();
        return this.getDefaultState()
                .with(DOWN, block == this || block == YNetMod.CONTROLLER || block instanceof BaseProvider)
                .with(UP, block2 == this || block2 == YNetMod.CONTROLLER || block2 instanceof BaseProvider)
                .with(NORTH, block3 == this || block3 == YNetMod.CONTROLLER || block3 instanceof BaseProvider)
                .with(EAST, block4 == this || block4 == YNetMod.CONTROLLER || block4 instanceof BaseProvider)
                .with(SOUTH, block5 == this || block5 == YNetMod.CONTROLLER || block5 instanceof BaseProvider)
                .with(WEST, block6 == this || block6 == YNetMod.CONTROLLER || block6 instanceof BaseProvider)
                .with(DOWN_CABLE, block == YNetMod.CABLE)
                .with(UP_CABLE, block2 == YNetMod.CABLE)
                .with(NORTH_CABLE, block3 == YNetMod.CABLE)
                .with(EAST_CABLE, block4 == YNetMod.CABLE)
                .with(SOUTH_CABLE, block5 == YNetMod.CABLE)
                .with(WEST_CABLE, block6 == YNetMod.CABLE);
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction facing) {
        return state.getWeakRedstonePower(world, pos, facing);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction facing) {
        return ((ConnectorBlockEntity)world.getBlockEntity(pos)).getRedstonePower(facing);
    }

    public void setRedstoneOutput(Direction facing, BlockView world, BlockPos pos, int strength) {
        ((ConnectorBlockEntity)world.getBlockEntity(pos)).setRedstonePower(facing, strength);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        Set<BlockPos> controllers = new HashSet<>();

        Network.getConnectedControllers(world, pos, controllers);

        System.out.println("Controllers: " + controllers);  // Empty?
        for (BlockPos p : controllers){
            ControllerBlockEntity be = (ControllerBlockEntity)world.getBlockEntity(p);
            be.updateNetwork();
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        Set<BlockPos> controllers = new HashSet<>();

        // TODO: Find a better way to do this instead of a BFS through the world
        Network.getConnectedControllers(world, pos, controllers);

        System.out.println("Controllers: " + controllers);
        for (BlockPos p : controllers){
            ControllerBlockEntity be = (ControllerBlockEntity)world.getBlockEntity(p);
            // be.network.connectors.add(p);
            Set<BlockPos> known = new HashSet<>();
            known.addAll(be.network.cables);
            known.addAll(be.network.connectors);
            Network.getConnectedBlocks(world, pos, known, be.network.cables, be.network.connectors);
        }
    }

    public int getRedstoneOutput(Direction facing, BlockView world, BlockPos pos){
        BlockState state = world.getBlockState(pos.offset(facing));
        return state.getBlock().getWeakRedstonePower(state, world, pos, facing);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView view) {
        return new ConnectorBlockEntity();
    }
}
