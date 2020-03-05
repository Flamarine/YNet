package com.martmists.ynet.event;

import com.martmists.ynet.api.BaseProvider;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

import java.lang.reflect.Array;
import java.util.Arrays;

public interface ProviderTickCallback<T extends BaseProvider> {
    // DO NOT OVERRIDE
    default T[] createArray(Class<T> clazz) { return (T[])Array.newInstance(clazz, 0); }
    ActionResult interact(T[] listeners, ControllerBlockEntity be);
}
