package com.martmists.ynet.network;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class ConnectorConfiguration {
    public State state = State.DISABLED;
    public BlockPos providerPos;
    public int priority;

    // For items
    public Item[] filter;

    public static enum State {
        DISABLED,
        INPUT,
        OUTPUT
    }
}
