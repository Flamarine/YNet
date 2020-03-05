package com.martmists.ynet.blocks;

import com.martmists.ynet.blockentities.ControllerBlockEntity;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;

public class ControllerBlock extends BlockWithEntity {
    public ControllerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView view) {
        return new ControllerBlockEntity();
    }
}
