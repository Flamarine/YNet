package com.martmists.ynet.event;

import com.martmists.ynet.api.BaseProvider;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import com.martmists.ynet.network.ConnectorConfiguration;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public interface ProviderTickCallback<T extends BaseProvider> {
    void interact(Set<ConnectorConfiguration> listeners, ControllerBlockEntity be);
}
