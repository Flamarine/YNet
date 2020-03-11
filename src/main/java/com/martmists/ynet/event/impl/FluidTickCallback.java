package com.martmists.ynet.event.impl;

import com.martmists.ynet.api.FluidProvider;
import com.martmists.ynet.api.ItemProvider;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import com.martmists.ynet.event.ProviderTickCallback;
import com.martmists.ynet.network.ConnectorConfiguration;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class FluidTickCallback implements ProviderTickCallback<FluidProvider> {
    @Override
    public void interact(Set<ConnectorConfiguration> listeners, ControllerBlockEntity be) {
        // Simple tuple class
        class Entry {
            private final BlockPos pos;
            private final FluidProvider provider;
            private final Fluid fluid;
            private int amount;

            Entry(BlockPos pos, FluidProvider provider, Fluid fluid, int amount) {
                this.pos = pos;
                this.provider = provider;
                this.fluid = fluid;
                this.amount = amount;
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
                    FluidProvider p = (FluidProvider) world.getBlockState(config.providerPos).getBlock();
                    Fluid f = p.getFluidOutput(world, config.providerPos);
                    if (f != Fluids.EMPTY) {
                        entries.add(new Entry(config.providerPos, p, f, p.getFluidOutputCount(world, config.providerPos)));
                    }
                });

        if (entries.isEmpty()) {
            return;
        }

        Entry e = entries.get(0);

        Map<FluidProvider, Integer> fluidsStored = new HashMap<>();
        Map<FluidProvider, Integer> fluidsRemoved = new HashMap<>();

        while (!entries.isEmpty()) {
            if (e.amount <= 0){
                try {
                    e = entries.get(1);
                } catch (IndexOutOfBoundsException exc) {
                    break;
                }
                entries.remove(0);
            }

            fluidsRemoved.putIfAbsent(e.provider, 0);

            if (fluidsRemoved.get(e.provider) >= 1000) {
                e.amount = 0;
                continue;
            }

            boolean found = false;
            for (ConnectorConfiguration receiverConfig : takeItems){
                FluidProvider receiver = (FluidProvider) world.getBlockState(receiverConfig.providerPos).getBlock();
                fluidsStored.putIfAbsent(receiver, 0);
                int count = Math.min(
                        receiver.getFluidInputCount(world, receiverConfig.providerPos, e.fluid),
                        Math.min(
                                1000 - fluidsStored.get(receiver),
                                1000 - fluidsRemoved.get(e.provider)));

                if (receiverConfig.filter != null) {
                    Entry fe = e;
                    if (Arrays.stream(receiverConfig.filter).noneMatch((obj) -> obj == fe.fluid)){
                        continue;
                    }
                }

                if (count > 0){
                    fluidsStored.put(receiver, fluidsStored.get(receiver) + count);
                    fluidsRemoved.put(e.provider, fluidsRemoved.get(e.provider) + count);
                    int finalCount = e.amount - count;
                    e.amount = count;
                    e.provider.outputFluid(world, e.pos, e.fluid, e.amount);
                    receiver.inputFluid(world, e.pos, e.fluid, e.amount);
                    e.amount = finalCount;
                    found = true;
                }
            }

            if (!found) {
                // Remove next
                e.amount = 0;
            }
        }
    }
}
