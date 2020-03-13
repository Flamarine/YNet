package com.martmists.ynet.blockentities;

import com.martmists.ynet.YNetMod;
import com.martmists.ynet.api.BaseProvider;
import com.martmists.ynet.event.ProviderTickCallback;
import com.martmists.ynet.network.Channel;
import com.martmists.ynet.network.ConnectorConfiguration;
import com.martmists.ynet.network.Network;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ControllerBlockEntity extends BlockEntity implements Tickable {
    public Network network;
    public Channel[] channels = new Channel[9];

    public ControllerBlockEntity() {
        super(YNetMod.CONTROLLER_BE);
        this.network = new Network();
    }

    public void updateNetwork() {
        network.reloadAllNodes(world);
    }

    private List<BlockPos> getConnectedProviders(Set<BlockPos> connectors) {
        List<BlockPos> providers = new ArrayList<>();
        for (BlockPos c : connectors) {
            for (BlockPos offset : new BlockPos[]{c.up(), c.down(), c.north(), c.east(), c.south(), c.west()}) {
                if (world.getBlockState(offset).getBlock() instanceof BaseProvider) {
                    providers.add(offset);
                }
            }
        }
        return providers;
    }

    @Override
    public void tick() {
        if (network.connectors == null) {
            register();
        }
        for (Channel ch : channels) {
            if (ch != null && ch.providerType != null) {
                ProviderTickCallback<?> callback = YNetMod.PROVIDERS.get(ch.providerType);
                callback.interact(ch.connectorSettings, this);
            }
        }
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        CompoundTag customData = tag.getCompound("controllerData");
        for (String key : customData.getKeys()) {
            int channelIndex = Integer.parseInt(key.substring(8));
            channels[channelIndex] = new Channel();
            CompoundTag cData = customData.getCompound(key);
            String cType = cData.getString("type");
            channels[channelIndex].providerType = YNetMod.PROVIDER_NAMES.entrySet().stream()
                    .filter(e -> e.getValue().equals(cType))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);

            if (channels[channelIndex].providerType == null) {
                channels[channelIndex] = null;
                continue;
            }

            ListTag connectors = cData.getList("connectors", NbtType.COMPOUND);
            channels[channelIndex].connectorSettings = connectors.stream().map(t -> {
                ConnectorConfiguration config = new ConnectorConfiguration();
                config.fromTag((CompoundTag) t);
                return config;
            }).collect(Collectors.toSet());
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        CompoundTag customData = new CompoundTag();
        for (int i = 0; i < 9; i++) {
            Channel c = channels[i];
            if (c != null) {
                CompoundTag cData = new CompoundTag();
                ListTag connectors = new ListTag();
                c.connectorSettings.forEach((s) -> {
                    s.toTag(connectors);
                });
                cData.put("connectors", connectors);
                String cType = YNetMod.PROVIDER_NAMES.getOrDefault(c.providerType, "null");
                cData.putString("type", cType);
                customData.put("channel_" + i, cData);
            }
        }
        tag.put("controllerData", customData);
        return super.toTag(tag);
    }

    @Override
    public void markDirty() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        CompoundTag tag = new CompoundTag();
        this.toTag(tag);
        buf.writeCompoundTag(tag);

        if (world.isClient()) {
            // Send C2S
            ClientSidePacketRegistry.INSTANCE.sendToServer(YNetMod.CONTROLLER_UPDATE_C2S, buf);
        } else {
            // Send S2C
            for (PlayerEntity player : world.getPlayers()) {
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, YNetMod.CONTROLLER_UPDATE_S2C, buf);
            }
        }
        super.markDirty();
    }

    public void sMarkDirty() {
        super.markDirty();
    }

    public void register() {
        network.setController(pos);
        updateNetwork();
    }
}
