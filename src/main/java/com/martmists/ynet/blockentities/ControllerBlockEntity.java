package com.martmists.ynet.blockentities;

import com.martmists.ynet.YNetMod;
import com.martmists.ynet.api.BaseProvider;
import com.martmists.ynet.event.ProviderTickCallback;
import com.martmists.ynet.network.Channel;
import com.martmists.ynet.network.Network;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class ControllerBlockEntity extends BlockEntity implements Tickable {
    public Network network;
    public Channel[] channels = new Channel[9];

    public ControllerBlockEntity() {
        super(YNetMod.CONTROLLER_BE);
        this.network = new Network();
        this.network.setController(pos);
    }

    public void updateNetwork() {
        network.reloadAllNodes(world);
    }

    private List<BlockPos> getConnectedProviders(Set<BlockPos> connectors) {
        List<BlockPos> providers = new ArrayList<>();
        for (BlockPos c : connectors){
            for (BlockPos offset : new BlockPos[]{ c.up(), c.down(), c.north(), c.east(), c.south(), c.west() }){
                if (world.getBlockState(offset).getBlock() instanceof BaseProvider) {
                    providers.add(offset);
                }
            }
        }
        return providers;
    }

    @Override
    public void tick() {
        if (network.cables == null) {
            network.setController(pos);
            updateNetwork();
        }
        for (Channel ch : channels) {
            if (ch != null && ch.providerType != null) {
                ProviderTickCallback<?> callback = YNetMod.PROVIDERS.get(ch.providerType);
                callback.interact(ch.connectorSettings, this);
            }
        }
    }

    // TODO:
    // - fromTag and toTag
    // - GUI
}
