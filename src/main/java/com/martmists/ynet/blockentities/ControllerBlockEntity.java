package com.martmists.ynet.blockentities;

import com.martmists.ynet.YNetMod;
import com.martmists.ynet.api.BaseProvider;
import com.martmists.ynet.blocks.CableBlock;
import com.martmists.ynet.blocks.ConnectorBlock;
import com.martmists.ynet.event.ProviderTickCallback;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class ControllerBlockEntity extends BlockEntity implements Tickable {
    public ControllerBlockEntity() {
        super(YNetMod.CONTROLLER_BE);
    }

    // TODO:
    // - Add configurable channels
    // - Add a way to get input/output blocks from said channel

    private Block[] getConnectedBlocks() {
        ArrayDeque<BlockPos> toSearch = new ArrayDeque<BlockPos>();
        toSearch.push(pos);
        List<BlockPos> searched = new ArrayList<>();
        searched.add(pos);
        List<BlockPos> providers = new ArrayList<>();
        while (!toSearch.isEmpty()){
            BlockPos p = toSearch.removeFirst();
            for (BlockPos p2 : Arrays.asList(p.up(), p.down(), p.north(), p.south(), p.east(), p.west())) {
                if (searched.contains(p2)) {
                    continue;
                }
                searched.add(p2);
                Block b = world.getBlockState(p2).getBlock();
                if (b == YNetMod.CABLE || b == YNetMod.CONNECTOR) {
                    toSearch.add(p2);
                } else if (b instanceof BaseProvider) {
                    providers.add(p2);
                }
            }
        }
        return (Block[]) providers.stream().map(p -> world.getBlockState(p).getBlock()).toArray();
    }

    @Override
    public void tick() {
        // TODO:
        // Collect all connected blocks
        Block[] blocks = getConnectedBlocks();

        Map<Class<? extends BaseProvider>, List<BaseProvider>> blockMap = new HashMap<>();
        for (Block b : blocks) {
            Class<?> bClass = b.getClass();
            if (!tMap.containsKey(bClass)){
                Set<Class<? extends BaseProvider>> set = new HashSet<>();
                findProviderTypes(bClass, set);
                tMap.put(bClass, set);
            }
            for (Class<? extends BaseProvider> clazz : tMap.get(bClass)){
                blockMap.putIfAbsent(clazz, new ArrayList<>());
                blockMap.get(clazz).add((BaseProvider)b);
            }
        }

        for (Class<? extends BaseProvider> clazz : blockMap.keySet()) {
            dispatch(clazz, blockMap.get(clazz));
        }
    }

    private <P extends BaseProvider> void dispatch(Class<? extends BaseProvider> clazz, List<P> blockMap) {
        ProviderTickCallback<P> provider = (ProviderTickCallback<P>) YNetMod.PROVIDERS.get(clazz);
        provider.interact(blockMap.toArray(provider.createArray((Class<P>) clazz)), this);

    }

    // Ugly hack by Pyrofab
    private static Map<Class<?>, Set<Class<? extends BaseProvider>>> tMap = new HashMap<>();
    private static void findProviderTypes(Class<?> cls, Set<Class<? extends BaseProvider>> ret) {
        tMap.putIfAbsent(cls, ret);
        if (cls != Object.class) {
            if (BaseProvider.class.isAssignableFrom(cls)) {
                ret.add((Class<? extends BaseProvider>) cls);
            }
            findProviderTypes(cls.getSuperclass(), ret);
            for (Class<?> itf : cls.getInterfaces()) {
                findProviderTypes(itf, ret);
            }
        }
    }
}
