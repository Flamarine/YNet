package com.martmists.ynet.blocks;

import com.martmists.ynet.YNetMod;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import com.martmists.ynet.network.Network;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class ControllerBlock extends BlockWithEntity {
    public ControllerBlock(Settings settings) {
        super(settings);
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
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        Network.networks.remove(pos);
    }
}
