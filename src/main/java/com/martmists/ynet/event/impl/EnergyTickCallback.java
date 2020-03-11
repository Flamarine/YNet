package com.martmists.ynet.event.impl;

import com.martmists.ynet.api.EnergyProvider;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import com.martmists.ynet.event.ProviderTickCallback;
import com.martmists.ynet.network.ConnectorConfiguration;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EnergyTickCallback implements ProviderTickCallback<EnergyProvider> {
    @Override
    public void interact(Set<ConnectorConfiguration> listeners, ControllerBlockEntity be) {
        World world = be.getWorld();
        List<ConnectorConfiguration> takeEnergy = listeners.stream()
                .filter(config -> config.state == ConnectorConfiguration.State.INPUT)
                .sorted((a, b) -> a.priority >= b.priority ? 0 : 1)
                .collect(Collectors.toList());
        List<ConnectorConfiguration> giveEnergy = listeners.stream()
                .filter(config -> config.state == ConnectorConfiguration.State.OUTPUT)
                .sorted((a, b) -> a.priority >= b.priority ? 0 : 1)
                .collect(Collectors.toList());

        double lastStored = 0.0;
        double lastTaken = 0.0;

        while (!takeEnergy.isEmpty() && !giveEnergy.isEmpty()) {

            ConnectorConfiguration receiverConfig = takeEnergy.get(0);
            ConnectorConfiguration providerConfig = giveEnergy.get(0);
            EnergyProvider receiver = (EnergyProvider) world.getBlockState(receiverConfig.providerPos).getBlock();
            EnergyProvider provider = (EnergyProvider) world.getBlockState(providerConfig.providerPos).getBlock();

            if (lastStored >= receiver.getEnergyInputLimit(world, receiverConfig.providerPos)) {
                lastStored = 0.0;
                try {
                    receiverConfig = takeEnergy.get(1);
                } catch (IndexOutOfBoundsException exc) {
                    break;
                }
                receiver = (EnergyProvider) world.getBlockState(receiverConfig.providerPos);
                takeEnergy.remove(0);
            }

            if (lastTaken >= provider.getEnergyOutputLimit(world, providerConfig.providerPos)) {
                lastTaken = 0.0;
                try {
                    providerConfig = giveEnergy.get(1);
                } catch (IndexOutOfBoundsException exc) {
                    break;
                }
                provider = (EnergyProvider) world.getBlockState(providerConfig.providerPos);
                giveEnergy.remove(0);
            }

            double toTransfer = Math.min(
                    receiver.getEnergyInputLimit(world, receiverConfig.providerPos),
                    provider.getEnergyOutputLimit(world, providerConfig.providerPos));

            provider.outputEnergy(world, providerConfig.providerPos, toTransfer);
            receiver.inputEnergy(world, receiverConfig.providerPos, toTransfer);
            lastStored += toTransfer;
            lastTaken += toTransfer;
        }
    }
}
