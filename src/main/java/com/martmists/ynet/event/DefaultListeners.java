package com.martmists.ynet.event;

import com.martmists.ynet.api.EnergyProvider;
import com.martmists.ynet.api.ItemProvider;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import com.martmists.ynet.network.ConnectorConfiguration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultListeners {
    public static void handleEnergyChannel(Set<ConnectorConfiguration> listeners, ControllerBlockEntity be) {
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
            EnergyProvider receiver = (EnergyProvider) world.getBlockState(receiverConfig.providerPos);
            EnergyProvider provider = (EnergyProvider) world.getBlockState(providerConfig.providerPos);

            if (lastStored >= receiver.getEnergyInputLimit(world, receiverConfig.providerPos)) {
                lastStored = 0.0;
                receiverConfig = takeEnergy.get(1);
                receiver = (EnergyProvider) world.getBlockState(receiverConfig.providerPos);
                takeEnergy.remove(0);
            }

            if (lastTaken >= provider.getEnergyOutputLimit(world, providerConfig.providerPos)) {
                lastTaken = 0.0;
                providerConfig = giveEnergy.get(1);
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

    public static void handleItemChannel(Set<ConnectorConfiguration> listeners, ControllerBlockEntity be) {
        // Simple tuple class
        class Entry {
            private final BlockPos pos;
            private final ItemProvider provider;
            private final ItemStack items;

            Entry(BlockPos pos, ItemProvider provider, ItemStack items) {
                this.pos = pos;
                this.provider = provider;
                this.items = items;
            }
        }

        // TODO: Figure this shit out
        World world = be.getWorld();
        List<ConnectorConfiguration> takeItems = listeners.stream()
                .filter(config -> config.state == ConnectorConfiguration.State.INPUT)
                .sorted((a, b) -> a.priority >= b.priority ? 0 : 1)
                .collect(Collectors.toList());
        List<Entry> entries = new ArrayList<>();
        listeners.stream()
                .filter(config -> config.state == ConnectorConfiguration.State.OUTPUT)
                .sorted((a, b) -> a.priority >= b.priority ? 0 : 1)
                .forEach(config -> {
                    ItemProvider p = (ItemProvider) world.getBlockState(config.providerPos);
                    for (ItemStack s : p.getItemOutputStacks(world, config.providerPos)){
                        entries.add(new Entry(config.providerPos, p, s));
                    }
                });

        if (entries.isEmpty()) {
            return;
        }

        Entry e = entries.get(0);

        Map<ItemProvider, Integer> itemsStored = new HashMap<>();

        while (!entries.isEmpty()) {
            if (e.items.getCount() <= 0){
                e = entries.get(1);
                entries.remove(0);
            }

            boolean found = false;
            for (ConnectorConfiguration  receiverConfig : takeItems){
                ItemProvider receiver = (ItemProvider) world.getBlockState(receiverConfig.providerPos);
                itemsStored.putIfAbsent(receiver, 0);
                int count = receiver.getItemInputCount(world, receiverConfig.providerPos, e.items);
                if (count > 0){
                    // TODO: Ensure we don't input more than 64 items per receiver
                    // We can remove these ItemProviders
                    // Also: Use filters
                    int finalCount = e.items.getCount() - count;
                    e.items.setCount(count);
                    e.provider.outputItem(world, e.pos, e.items);
                    receiver.inputItem(world, e.pos, e.items);
                    e.items.setCount(finalCount);
                    found = true;
                }
            }

            if (!found) {
                // Remove next
                e.items.setCount(0);
            }
        }
    }
}
