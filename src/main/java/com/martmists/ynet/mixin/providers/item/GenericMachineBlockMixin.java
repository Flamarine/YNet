package com.martmists.ynet.mixin.providers.item;

import com.martmists.ynet.api.ItemProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import reborncore.common.util.DefaultedListCollector;
import techreborn.blockentity.machine.GenericMachineBlockEntity;
import techreborn.blocks.GenericMachineBlock;

import java.util.Arrays;
import java.util.List;

@Mixin(GenericMachineBlock.class)
public class GenericMachineBlockMixin implements ItemProvider {

    @Override
    public int getItemInputCount(BlockView world, BlockPos pos, ItemStack itemStack) {
        GenericMachineBlockEntity be = getBlockEntity(world, pos);
        if (hasInventory(be)) {
            List<ItemStack> stacks = getInputStacks(be);
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
        return 0;
    }

    @Override
    public void inputItem(BlockView world, BlockPos pos, ItemStack itemStack) {
        List<ItemStack> stacks = getInputStacks(getBlockEntity(world, pos));
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
        return getOutputStacks(getBlockEntity(world, pos)).stream().filter(stack -> !stack.isEmpty()).toArray(ItemStack[]::new);
    }

    @Override
    public void outputItem(BlockView world, BlockPos pos, ItemStack itemStack) {
        List<ItemStack> stacks = getOutputStacks(getBlockEntity(world, pos));
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
            if (stack.isEmpty()) {
                stacks.set(i, new ItemStack(itemStack.getItem(), outputCount));
                return;
            }
            i++;
        }
    }

    private GenericMachineBlockEntity getBlockEntity(BlockView world, BlockPos pos) {
        return (GenericMachineBlockEntity) world.getBlockEntity(pos);
    }

    private List<ItemStack> getInputStacks(GenericMachineBlockEntity be) {
        return Arrays.stream(be.crafter.inputSlots).mapToObj(be::getInvStack).collect(DefaultedListCollector.toList());
    }

    private List<ItemStack> getOutputStacks(GenericMachineBlockEntity be) {
        return Arrays.stream(be.crafter.outputSlots).mapToObj(be::getInvStack).collect(DefaultedListCollector.toList());
    }

    private boolean hasInventory(GenericMachineBlockEntity be) {
        return be.getOptionalInventory().isPresent();
    }
}
