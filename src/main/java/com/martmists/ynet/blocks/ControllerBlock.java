package com.martmists.ynet.blocks;

import com.martmists.ynet.api.BaseProvider;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

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
        if (world.isClient) {
            // Open screen
            // for now, print all connected providers
            Set<BlockPos> providers = ((ControllerBlockEntity)world.getBlockEntity(pos)).network.getProviders(world);
            System.out.println("Start of list");
            for (BlockPos p : providers) {
                // TODO: Able to find providers that are no longer connected (e.g. cable was broken)
                System.out.println("Provider found at " + p + ": " + world.getBlockState(p).getBlock());
            }
            System.out.println("End of list");
        }

        return ActionResult.SUCCESS;
    }
}
