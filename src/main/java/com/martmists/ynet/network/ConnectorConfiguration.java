package com.martmists.ynet.network;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ConnectorConfiguration {
    public ConnectorConfiguration() {
        state = State.DISABLED;
        priority = 0;
    }

    public State state;
    public BlockPos providerPos;
    public int priority;

    public Item[] filter;

    public enum State {
        DISABLED,
        INPUT,
        OUTPUT
    }

    public void toTag(ListTag tag) {
        CompoundTag data = new CompoundTag();
        ListTag l = new ListTag();

        Arrays.stream(filter).forEach(i -> {
            CompoundTag t = new CompoundTag();
            new ItemStack(i).toTag(t);
            l.add(t);
        });

        data.putInt("priority", priority);
        data.putInt("state", state.ordinal());
        data.put("filter", l);
        data.putIntArray("pos", new int[] { providerPos.getX(), providerPos.getY(), providerPos.getZ() });
    }

    public void fromTag(CompoundTag tag) {
        // Same here
        priority = tag.getInt("priority");
        int[] pos = tag.getIntArray("pos");
        providerPos = new BlockPos(pos[0], pos[1], pos[2]);
        state = State.values()[tag.getInt("state")];
        filter = tag.getList("filter", NbtType.COMPOUND).stream()
                .map(t -> ItemStack.fromTag((CompoundTag)t).getItem())
                .toArray(Item[]::new);
    }
}
