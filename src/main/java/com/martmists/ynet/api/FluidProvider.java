package com.martmists.ynet.api;

import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface FluidProvider extends BaseProvider {
    int getFluidOutputCount(BlockView world, BlockPos pos);

    Fluid getFluidOutput(BlockView world, BlockPos pos);

    int getFluidInputCount(BlockView world, BlockPos pos, Fluid fluid);

    void outputFluid(BlockView world, BlockPos pos, Fluid fluid, int amount);

    void inputFluid(BlockView world, BlockPos pos, Fluid fluid, int amount);
}
