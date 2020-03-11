package com.martmists.ynet;

import com.martmists.ynet.screens.ControllerScreen;
import com.martmists.ynet.containers.ControllerContainer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry;
import net.minecraft.util.Identifier;

public class YNetModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenProviderRegistry.INSTANCE.registerFactory(new Identifier("ynet:controller"), (c) -> {
            return new ControllerScreen((ControllerContainer) c);
        });
    }
}
