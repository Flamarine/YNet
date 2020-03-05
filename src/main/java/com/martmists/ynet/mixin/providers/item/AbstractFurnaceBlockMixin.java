package com.martmists.ynet.mixin.providers.item;

import com.martmists.ynet.api.ItemProvider;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceBlock.class)
public abstract class AbstractFurnaceBlockMixin implements ItemProvider {
    @Override
    public int getItemInputCount(BlockView world, BlockPos pos, ItemStack itemStack) {
        AbstractFurnaceBlockEntity be = getBlockEntity(world, pos);
        if (AbstractFurnaceBlockEntity.canUseAsFuel(itemStack)){
            // put in fuel slot
            ItemStack stack = be.getInvStack(1);
            if (stack.isEmpty()){
                return itemStack.getCount();
            }
            return stack.getMaxCount() - stack.getCount();
        } else {
            ItemStack stack = be.getInvStack(0);
            if (stack.isEmpty()){
                return itemStack.getCount();
            } else {
                if (stack.getItem() == itemStack.getItem()){
                    return stack.getMaxCount() - stack.getCount();
                } else {
                    return 0;
                }
            }
        }
    }

    @Override
    public void inputItem(BlockView world, BlockPos pos, ItemStack itemStack) {
        AbstractFurnaceBlockEntity be = getBlockEntity(world, pos);
        if (AbstractFurnaceBlockEntity.canUseAsFuel(itemStack)){
            ItemStack stack = be.getInvStack(1);
            if (stack.isEmpty()){
                be.setInvStack(1, itemStack);
            } else {
                stack.setCount(itemStack.getCount() + stack.getCount());
            }
        } else {
            ItemStack stack = be.getInvStack(0);
            if (stack.isEmpty()){
                be.setInvStack(0, itemStack);
            } else {
                stack.setCount(itemStack.getCount() + stack.getCount());
            }
        }
    }

    @Override
    public ItemStack[] getItemOutputStacks(BlockView world, BlockPos pos) {
        return new ItemStack[] { getBlockEntity(world, pos).getInvStack(2) };
    }

    @Override
    public void outputItem(BlockView world, BlockPos pos, ItemStack itemStack) {
        AbstractFurnaceBlockEntity be = getBlockEntity(world, pos);
        ItemStack stack = be.getInvStack(2);
        if (itemStack.getCount() == stack.getCount()){
            be.setInvStack(2, ItemStack.EMPTY);
        } else {
            stack.setCount(stack.getCount() - itemStack.getCount());
        }
    }

    private AbstractFurnaceBlockEntity getBlockEntity(BlockView world, BlockPos pos){
        return (AbstractFurnaceBlockEntity) world.getBlockEntity(pos);
    }
}
