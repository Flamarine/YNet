package com.martmists.ynet;

import com.martmists.ynet.blockentities.ControllerBlockEntity;
import com.martmists.ynet.containers.ControllerContainer;
import com.martmists.ynet.screens.ControllerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class YNetModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenProviderRegistry.INSTANCE.registerFactory(new Identifier("ynet:controller"), (c) -> {
            return new ControllerScreen((ControllerContainer) c);
        });
        ClientSidePacketRegistry.INSTANCE.register(YNetMod.CONTROLLER_UPDATE_S2C, (packetContext, attachedData) -> {
            BlockPos pos = attachedData.readBlockPos();
            CompoundTag tag = attachedData.readCompoundTag();
            packetContext.getTaskQueue().execute(() -> {
                ControllerBlockEntity be = (ControllerBlockEntity) MinecraftClient.getInstance().world.getBlockEntity(pos);
                be.fromTag(tag);
                be.sMarkDirty();
            });
        });
    }
}
