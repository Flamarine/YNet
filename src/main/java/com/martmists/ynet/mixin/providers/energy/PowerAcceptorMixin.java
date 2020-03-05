package com.martmists.ynet.mixin.providers.energy;

import com.martmists.ynet.api.EnergyProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import team.reborn.energy.EnergySide;
import techreborn.blocks.GenericMachineBlock;
import techreborn.blocks.generator.GenericGeneratorBlock;
import techreborn.blocks.storage.energy.EnergyStorageBlock;


@Mixin({GenericMachineBlock.class, GenericGeneratorBlock.class, EnergyStorageBlock.class})
public class PowerAcceptorMixin implements EnergyProvider {
    @Override
    public double getEnergyInputLimit(BlockView world, BlockPos pos) {
        PowerAcceptorBlockEntity be = getBlockEntity(world, pos);
        if (be == null) {
            return 0;
        }
        return be.canAcceptEnergy(null) ?
                Math.min(
                    be.getMaxStoredPower()-be.getStored(EnergySide.UNKNOWN),
                    be.getMaxInput(EnergySide.UNKNOWN)) :
                0;
    }

    @Override
    public void inputEnergy(BlockView world, BlockPos pos, double energy) {
        PowerAcceptorBlockEntity be = getBlockEntity(world, pos);
        be.setEnergy(be.getEnergy()+energy);
    }

    @Override
    public double getEnergyOutputLimit(BlockView world, BlockPos pos) {
        PowerAcceptorBlockEntity be = getBlockEntity(world, pos);
        if (be == null) {
            return 0;
        }
        return be.canProvideEnergy(null) ?
                Math.min(
                    be.getStored(EnergySide.UNKNOWN),
                    be.getMaxOutput(EnergySide.UNKNOWN)) :
                0;
    }

    @Override
    public void outputEnergy(BlockView world, BlockPos pos, double energy) {
        PowerAcceptorBlockEntity be = getBlockEntity(world, pos);
        be.setEnergy(be.getEnergy()-energy);
    }

    private PowerAcceptorBlockEntity getBlockEntity(BlockView world, BlockPos pos){
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof PowerAcceptorBlockEntity){
            return (PowerAcceptorBlockEntity) be;
        }
        return null;
    }
}
