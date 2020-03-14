package com.martmists.ynet.mixin.providers.item;

import com.martmists.ynet.api.ItemProvider;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractFurnaceBlock.class)
public abstract class AbstractFurnaceBlockMixin implements ItemProvider {
    @Override
    public int getItemInputCount(BlockView world, BlockPos pos, ItemStack itemStack) {
        AbstractFurnaceBlockEntity be = getBlockEntity(world, pos);
        ItemStack stack =  be.getInvStack(AbstractFurnaceBlockEntity.canUseAsFuel(itemStack) ? 1 : 0);
        // put in fuel slot
        if (stack.isEmpty()) {
            return itemStack.getCount();
        } else {
            if (stack.getItem() == itemStack.getItem()) {
                return Math.min(stack.getMaxCount() - stack.getCount(), itemStack.getCount());
            } else {
                return 0;
            }
        }
    }

    @Override
    public void inputItem(BlockView world, BlockPos pos, ItemStack itemStack) {
        AbstractFurnaceBlockEntity be = getBlockEntity(world, pos);
        if (AbstractFurnaceBlockEntity.canUseAsFuel(itemStack)) {
            ItemStack stack = be.getInvStack(1);
            if (stack.isEmpty()) {
                be.setInvStack(1, new ItemStack(itemStack.getItem(), itemStack.getCount()));
            } else {
                stack.setCount(itemStack.getCount() + stack.getCount());
            }
        } else {
            ItemStack stack = be.getInvStack(0);
            if (stack.isEmpty()) {
                be.setInvStack(0, new ItemStack(itemStack.getItem(), itemStack.getCount()));
            } else {
                stack.setCount(itemStack.getCount() + stack.getCount());
            }
        }
    }

    @Override
    public ItemStack[] getItemOutputStacks(BlockView world, BlockPos pos) {
        return new ItemStack[]{ getBlockEntity(world, pos).getInvStack(2) };
    }

    @Override
    public void outputItem(BlockView world, BlockPos pos, ItemStack itemStack) {
        AbstractFurnaceBlockEntity be = getBlockEntity(world, pos);
        ItemStack stack = be.getInvStack(2);
        if (itemStack.getCount() == stack.getCount()) {
            be.setInvStack(2, ItemStack.EMPTY);
        } else {
            stack.setCount(stack.getCount() - itemStack.getCount());
        }
    }

    private AbstractFurnaceBlockEntity getBlockEntity(BlockView world, BlockPos pos) {
        return (AbstractFurnaceBlockEntity) world.getBlockEntity(pos);
    }
}
