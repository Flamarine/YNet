package com.martmists.ynet.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface ItemProvider extends BaseProvider {
    /**
     * @param world     The world the block resides in
     * @param pos       The BlockPos of the block
     * @param itemStack The ItemStack to attempt to input
     * @return The amount of items from the stack that can be input
     *          Should at most be itemStack.getCount()
     */
    int getItemInputCount(BlockView world, BlockPos pos, ItemStack itemStack);

    /**
     * @param world     The world the block resides in
     * @param pos       The BlockPos of the block
     * @param itemStack The ItemStack to add to the block
     */
    void inputItem(BlockView world, BlockPos pos, ItemStack itemStack);

    /**
     * @param world The world the block resides in
     * @param pos   The BlockPos of the block
     * @return The ItemStacks that can be output
     */
    ItemStack[] getItemOutputStacks(BlockView world, BlockPos pos);

    /**
     * @param world     The world the block resides in
     * @param pos       The BlockPos of the block
     * @param itemStack the ItemStack to remove from the container
     *                    May not be an exact existing stack, make sure to check the count
     */
    void outputItem(BlockView world, BlockPos pos, ItemStack itemStack);
}
