package com.martmists.ynet.network;

import net.minecraft.util.math.BlockPos;

public class ConnectorConfiguration {
    public ConnectorConfiguration() {
        state = State.DISABLED;
        priority = 0;
    }

    public State state;
    public BlockPos providerPos;
    public int priority;

    // For items/fluids/etc
    public Object[] filter;

    public enum State {
        DISABLED,
        INPUT,
        OUTPUT
    }
}
