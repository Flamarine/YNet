package com.martmists.ynet.blocks;

import com.martmists.ynet.YNetMod;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import com.martmists.ynet.network.Network;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class ControllerBlock extends FacingBlock implements BlockEntityProvider {
    public ControllerBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection());
    }

    @Override
    public BlockEntity createBlockEntity(BlockView view) {
        return new ControllerBlockEntity();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {

            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(pos);
            CompoundTag tag = new CompoundTag();
            world.getBlockEntity(pos).toTag(tag);
            buf.writeCompoundTag(tag);
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, YNetMod.CONTROLLER_UPDATE_S2C, buf);

            ContainerProviderRegistry.INSTANCE.openContainer(new Identifier("ynet:controller"), player, (buffer) -> {
                buffer.writeBlockPos(pos);
            });
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        Network.networks.remove(pos);
    }
}
