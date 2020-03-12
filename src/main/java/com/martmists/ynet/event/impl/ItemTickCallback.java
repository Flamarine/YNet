package com.martmists.ynet.event.impl;

import com.martmists.ynet.api.ItemProvider;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import com.martmists.ynet.event.ProviderTickCallback;
import com.martmists.ynet.network.ConnectorConfiguration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class ItemTickCallback implements ProviderTickCallback<ItemProvider> {
    @Override
    public void interact(Set<ConnectorConfiguration> listeners, ControllerBlockEntity be) {
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
                    ItemProvider p = (ItemProvider) world.getBlockState(config.providerPos).getBlock();
                    for (ItemStack s : p.getItemOutputStacks(world, config.providerPos)) {
                        entries.add(new Entry(config.providerPos, p, s));
                    }
                });

        if (entries.isEmpty()) {
            return;
        }

        Entry e = entries.get(0);

        Map<ItemProvider, Integer> itemsStored = new HashMap<>();
        Map<ItemProvider, Integer> itemsRemoved = new HashMap<>();

        while (!entries.isEmpty()) {
            if (e.items.getCount() <= 0) {
                try {
                    e = entries.get(1);
                } catch (IndexOutOfBoundsException exc) {
                    break;
                }
                entries.remove(0);
            }

            itemsRemoved.putIfAbsent(e.provider, 0);

            if (itemsRemoved.get(e.provider) >= 64) {
                e.items.setCount(0);
                continue;
            }

            boolean found = false;
            for (ConnectorConfiguration receiverConfig : takeItems) {
                ItemProvider receiver = (ItemProvider) world.getBlockState(receiverConfig.providerPos).getBlock();
                itemsStored.putIfAbsent(receiver, 0);
                int count = Math.min(
                        receiver.getItemInputCount(world, receiverConfig.providerPos, e.items),
                        Math.min(
                                64 - itemsStored.get(receiver),
                                64 - itemsRemoved.get(e.provider)));

                if (receiverConfig.filter != null) {
                    Entry fe = e;
                    if (Arrays.stream(receiverConfig.filter).noneMatch((obj) -> obj == fe.items.getItem())) {
                        continue;
                    }
                }

                if (count > 0) {
                    itemsStored.put(receiver, itemsStored.get(receiver) + count);
                    itemsRemoved.put(e.provider, itemsRemoved.get(e.provider) + count);
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
