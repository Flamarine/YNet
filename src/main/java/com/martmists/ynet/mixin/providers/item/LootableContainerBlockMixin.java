package com.martmists.ynet.mixin.providers.item;

import com.martmists.ynet.api.ItemProvider;
import com.martmists.ynet.mixin.accessors.InventoryStacksAccessor;
import net.minecraft.block.*;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(value = {
        ChestBlock.class, TrappedChestBlock.class,
        ShulkerBoxBlock.class, DispenserBlock.class,
        HopperBlock.class, BarrelBlock.class})
public abstract class LootableContainerBlockMixin implements ItemProvider {

    @Override
    public int getItemInputCount(BlockView world, BlockPos pos, ItemStack itemStack) {
        List<ItemStack> stacks = ((InventoryStacksAccessor) getBlockEntity(world, pos)).callGetInvStackList();
        if (stacks.stream().anyMatch(ItemStack::isEmpty)) {
            return itemStack.getCount();
        }
        int available = 0;
        for (ItemStack stack : stacks) {
            if (stack.getItem() == itemStack.getItem()) {
                available += stack.getMaxCount() - stack.getCount();
            }
        }
        return Math.min(available, itemStack.getCount());
    }

    @Override
    public void inputItem(BlockView world, BlockPos pos, ItemStack itemStack) {
        List<ItemStack> stacks = ((InventoryStacksAccessor) getBlockEntity(world, pos)).callGetInvStackList();
        int inputCount = itemStack.getCount();
        for (ItemStack stack : stacks) {
            if (stack.getItem() == itemStack.getItem()) {
                int available = stack.getMaxCount() - stack.getCount();
                int inputting = Math.min(available, inputCount);
                inputCount -= inputting;
                stack.setCount(stack.getCount() + inputting);
            }
            if (inputCount <= 0) {
                return;
            }
        }
        if (inputCount > 0) {
            int i = 0;
            for (ItemStack stack : stacks) {
                if (stack.isEmpty()) {
                    stacks.set(i, new ItemStack(itemStack.getItem(), inputCount));
                    return;
                }
                i++;
            }
        }
    }

    @Override
    public ItemStack[] getItemOutputStacks(BlockView world, BlockPos pos) {
        return ((InventoryStacksAccessor) getBlockEntity(world, pos)).callGetInvStackList().stream().filter(stack -> !stack.isEmpty()).toArray(ItemStack[]::new);
    }

    @Override
    public void outputItem(BlockView world, BlockPos pos, ItemStack itemStack) {
        List<ItemStack> stacks = ((InventoryStacksAccessor) getBlockEntity(world, pos)).callGetInvStackList();
        int outputCount = itemStack.getCount();
        int i = 0;
        for (ItemStack stack : stacks) {
            if (stack.getItem() == itemStack.getItem()) {
                int outputting = Math.min(outputCount, stack.getCount());
                stack.setCount(stack.getCount() - outputting);
                outputCount -= outputting;
                if (stack.getCount() == 0) {
                    stacks.set(i, ItemStack.EMPTY);
                }
                if (outputCount <= 0) {
                    return;
                }
            }
            i++;
        }
    }

    private LootableContainerBlockEntity getBlockEntity(BlockView world, BlockPos pos) {
        return (LootableContainerBlockEntity) world.getBlockEntity(pos);
    }
}
