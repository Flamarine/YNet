package com.martmists.ynet.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface EnergyProvider extends BaseProvider {
    /**
     * @param world The world the block resides in
     * @param pos The BlockPos of the block
     * @return The maximum energy this block can receive
     */
    double getEnergyInputLimit(BlockView world, BlockPos pos);

    /**
     * @param world The world the block resides in
     * @param pos The BlockPos of the block
     * @param energy The amount of energy to be added to the machine
     */
    void inputEnergy(BlockView world, BlockPos pos, double energy);

    /**
     * @param world The world the block resides in
     * @param pos The BlockPos of the block
     * @return The maximum energy this block can output
     */
    double getEnergyOutputLimit(BlockView world, BlockPos pos);

    /**
     * @param world The world the block resides in
     * @param pos The BlockPos of the block
     * @param energy The amount of energy to be added to the machine
     */
    void outputEnergy(BlockView world, BlockPos pos, double energy);
}
