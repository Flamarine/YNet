package com.martmists.ynet.blockentities;

import com.martmists.ynet.YNetMod;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;

public class ConnectorBlockEntity extends BlockEntity {
    private Map<Direction, Integer> redstoneState = new HashMap<>();

    public ConnectorBlockEntity() {
        super(YNetMod.CONNECTOR_BE);
    }

    public void setRedstonePower(Direction d, int p) {
        redstoneState.put(d, p);
    }

    public int getRedstonePower(Direction d) {
        return redstoneState.getOrDefault(d, 0);
    }
}
