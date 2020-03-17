package com.martmists.ynet.mixin.providers.item;

import com.martmists.ynet.api.ItemProvider;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(BrewingStandBlock.class)
public class BrewingStandBlockMixin implements ItemProvider {
    // Potions: Slot 0-2
    // Ingredient: Slot 3
    // Blaze powder: Slot 4

    @Override
    public int getItemInputCount(BlockView world, BlockPos pos, ItemStack itemStack) {
        BrewingStandBlockEntity be = getBlockEntity(world, pos);

        int totalInsertable = 0;

        for (int i = 0; i < 5; i++) {
            if (be.canInsertInvStack(i, itemStack, null)) {
                ItemStack stack = be.getInvStack(i);
                totalInsertable += stack.getMaxCount() - stack.getCount();
            }
        }

        return Math.min(totalInsertable, itemStack.getCount());
    }

    @Override
    public void inputItem(BlockView world, BlockPos pos, ItemStack itemStack) {
        BrewingStandBlockEntity be = getBlockEntity(world, pos);
        int totalInserted = 0;
        for (int i = 0; i < 5; i++) {
            if (be.canInsertInvStack(i, itemStack, null)) {
                ItemStack stack = be.getInvStack(i);
                int insertible = Math.min(stack.getMaxCount() - stack.getCount(), itemStack.getCount() - totalInserted);
                stack.setCount(stack.getCount() + insertible);
                totalInserted += insertible;
            }
        }
    }

    @Override
    public ItemStack[] getItemOutputStacks(BlockView world, BlockPos pos) {
        BrewingStandBlockEntity be = getBlockEntity(world, pos);
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ItemStack stack = be.getInvStack(i);
            if (be.canExtractInvStack(i, stack, null)) {
                stacks.add(stack);
            }
        }
        return stacks.toArray(new ItemStack[0]);
    }

    @Override
    public void outputItem(BlockView world, BlockPos pos, ItemStack itemStack) {
        BrewingStandBlockEntity be = getBlockEntity(world, pos);
        int totalExtracted = 0;
        for (int i = 0; i < 5; i++) {
            if (be.canExtractInvStack(i, itemStack, null)) {
                ItemStack stack = be.getInvStack(i);
                int extractable = Math.min(stack.getCount(), itemStack.getCount() - totalExtracted);
                stack.setCount(stack.getCount() - extractable);
                totalExtracted += extractable;
            }
        }
    }

    private BrewingStandBlockEntity getBlockEntity(BlockView world, BlockPos pos) {
        return (BrewingStandBlockEntity) world.getBlockEntity(pos);
    }
}
