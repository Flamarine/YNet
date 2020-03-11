package com.martmists.ynet.mixin.providers.fluid;

import com.martmists.ynet.api.FluidProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import reborncore.common.blocks.BlockMachineBase;

@Mixin(BlockMachineBase.class)
public class BlockMachineBaseMixin implements FluidProvider {

    @Override
    public int getFluidOutputCount(BlockView world, BlockPos pos) {
        return 0;
    }

    @Override
    public Fluid getFluidOutput(BlockView world, BlockPos pos) {
        return null;
    }

    @Override
    public int getFluidInputCount(BlockView world, BlockPos pos, Fluid fluid) {
        return 0;
    }

    @Override
    public void outputFluid(BlockView world, BlockPos pos, Fluid fluid, int amount) {

    }

    @Override
    public void inputFluid(BlockView world, BlockPos pos, Fluid fluid, int amount) {

    }
}
