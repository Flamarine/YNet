package com.martmists.ynet.blocks;

import com.martmists.ynet.YNetMod;
import com.martmists.ynet.api.BaseProvider;
import com.sun.org.apache.xpath.internal.operations.Bool;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.ModifiableWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectorBlock extends ConnectingBlock {
    public static final IntProperty NORTH_REDSTONE = IntProperty.of("NORTH_REDSTONE", 0, 15);
    public static final IntProperty EAST_REDSTONE = IntProperty.of("EAST_REDSTONE", 0, 15);
    public static final IntProperty SOUTH_REDSTONE = IntProperty.of("SOUTH_REDSTONE", 0, 15);
    public static final IntProperty WEST_REDSTONE = IntProperty.of("WEST_REDSTONE", 0, 15);
    public static final IntProperty UP_REDSTONE = IntProperty.of("UP_REDSTONE", 0, 15);
    public static final IntProperty DOWN_REDSTONE = IntProperty.of("DOWN_REDSTONE", 0, 15);

    public ConnectorBlock(Settings settings) {
        super(0.1875F, settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(UP, false)
                .with(DOWN, false)
                .with(NORTH_REDSTONE, 0)
                .with(EAST_REDSTONE, 0)
                .with(SOUTH_REDSTONE, 0)
                .with(WEST_REDSTONE, 0)
                .with(UP_REDSTONE, 0)
                .with(DOWN_REDSTONE, 0)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
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
            return state.with(FACING_PROPERTIES.get(facing), block == this || block == YNetMod.CABLE || block instanceof BaseProvider);
        }
    }

    public <T extends BaseProvider> T[] getProviders(BlockView world, BlockPos pos) {
        List<T> providers = new ArrayList<>();
        for (BlockPos position : new BlockPos[]{ pos.up(), pos.down(), pos.north(), pos.east(), pos.south(), pos.west() }){
            Block b = world.getBlockState(position).getBlock();
            if (b instanceof BaseProvider){
                providers.add((T)b);
            }
        }
        return (T[])providers.toArray();
    }

    public BlockState withConnectionProperties(BlockView world, BlockPos pos) {
        Block block = world.getBlockState(pos.down()).getBlock();
        Block block2 = world.getBlockState(pos.up()).getBlock();
        Block block3 = world.getBlockState(pos.north()).getBlock();
        Block block4 = world.getBlockState(pos.east()).getBlock();
        Block block5 = world.getBlockState(pos.south()).getBlock();
        Block block6 = world.getBlockState(pos.west()).getBlock();
        return this.getDefaultState()
                .with(DOWN, block == this || block == YNetMod.CABLE || block instanceof BaseProvider)
                .with(UP, block2 == this || block2 == YNetMod.CABLE || block2 instanceof BaseProvider)
                .with(NORTH, block3 == this || block3 == YNetMod.CABLE || block3 instanceof BaseProvider)
                .with(EAST, block4 == this || block4 == YNetMod.CABLE || block4 instanceof BaseProvider)
                .with(SOUTH, block5 == this || block5 == YNetMod.CABLE || block5 instanceof BaseProvider)
                .with(WEST, block6 == this || block6 == YNetMod.CABLE || block6 instanceof BaseProvider);
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction facing) {
        return state.getWeakRedstonePower(world, pos, facing);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction facing) {
        return state.get(getProp(facing));
    }

    public void setRedstoneOutput(Direction facing, BlockView world, BlockPos pos, int strength) {

        ((ModifiableWorld)world).setBlockState(pos, world.getBlockState(pos).with(getProp(facing), strength), 3);
    }

    private IntProperty getProp(Direction facing) {
        IntProperty p;
        switch (facing){
            case DOWN:
                p = DOWN_REDSTONE;
                break;
            case UP:
                p = UP_REDSTONE;
                break;
            case NORTH:
                p = NORTH_REDSTONE;
                break;
            case SOUTH:
                p = SOUTH_REDSTONE;
                break;
            case WEST:
                p = WEST_REDSTONE;
                break;
            case EAST:
                p = EAST_REDSTONE;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + facing);
        }
        return p;
    }

    public int getRedstoneOutput(Direction facing, BlockView world, BlockPos pos){
        BlockState state = world.getBlockState(pos.offset(facing));
        return state.getBlock().getWeakRedstonePower(state, world, pos, facing);
    }
}
