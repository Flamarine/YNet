package com.martmists.ynet;

import com.martmists.ynet.api.BaseProvider;
import com.martmists.ynet.api.EnergyProvider;
import com.martmists.ynet.api.FluidProvider;
import com.martmists.ynet.api.ItemProvider;
import com.martmists.ynet.blockentities.ConnectorBlockEntity;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import com.martmists.ynet.blocks.CableBlock;
import com.martmists.ynet.blocks.ConnectorBlock;
import com.martmists.ynet.blocks.ControllerBlock;
import com.martmists.ynet.containers.ControllerContainer;
import com.martmists.ynet.event.impl.EnergyTickCallback;
import com.martmists.ynet.event.impl.FluidTickCallback;
import com.martmists.ynet.event.impl.ItemTickCallback;
import com.martmists.ynet.event.ProviderTickCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class YNetMod implements ModInitializer {
    public static ItemGroup YNET_GROUP = FabricItemGroupBuilder.build(
            new Identifier("ynet", "items"),
            () -> new ItemStack(
                    Registry.ITEM.get(
                            new Identifier("ynet", "cable")
                    )
            )
    );

    public static CableBlock CABLE = register("cable", new CableBlock(Block.Settings.of(Material.METAL)));
    public static ConnectorBlock CONNECTOR = register("connector", new ConnectorBlock(Block.Settings.of(Material.METAL)));
    public static ControllerBlock CONTROLLER = register("controller", new ControllerBlock(Block.Settings.of(Material.METAL)));

    public static BlockEntityType<ConnectorBlockEntity> CONNECTOR_BE = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            new Identifier("ynet", "connector"),
            BlockEntityType.Builder.create(ConnectorBlockEntity::new, CONNECTOR).build(null)
    );
    public static BlockEntityType<ControllerBlockEntity> CONTROLLER_BE = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            new Identifier("ynet", "controller"),
            BlockEntityType.Builder.create(ControllerBlockEntity::new, CONTROLLER).build(null)
    );

    // Internal use
    public static Map<Class<? extends BaseProvider>, ProviderTickCallback<? extends BaseProvider>> PROVIDERS = new HashMap<>();
    public static Map<Class<? extends BaseProvider>, String> PROVIDER_NAMES = new HashMap<>();
    public static Map<Class<? extends BaseProvider>, Integer> COLOR_MAP = new HashMap<>();

    @Override
    public void onInitialize() {
        System.out.println("YNet loading up!");
        register("ynet:item", 0xff489030, ItemProvider.class, new ItemTickCallback());
        register("ynet:fluid", 0xff0077be, FluidProvider.class, new FluidTickCallback());
        register("ynet:energy", 0xfffffe00, EnergyProvider.class, new EnergyTickCallback());
        // TODO:
        // - Add support for configuring redstone signals on connectorss

        ContainerProviderRegistry.INSTANCE.registerFactory(new Identifier("ynet:controller"), (syncId, id, player, buf) -> {
            return new ControllerContainer(syncId, player.inventory, buf);
        });
    }

    static <T extends Block> T register(String name, T block) {
        return register(name, block, new BlockItem(block, new Item.Settings().group(YNET_GROUP)));
    }

    static <T extends Block> T register(String name, T block, BlockItem bl) {
        T b = Registry.register(Registry.BLOCK, new Identifier("ynet", name), block);
        register(name, bl);
        return b;
    }

    static <T extends Item> T register(String name, T item) {
        return Registry.register(Registry.ITEM, new Identifier("ynet", name), item);
    }

    static <T extends BaseProvider> ProviderTickCallback<T> register(String name, int color, Class<T> clazz, ProviderTickCallback<T> callback) {
        PROVIDER_NAMES.put(clazz, name.replace(":", "."));
        PROVIDERS.put(clazz, callback);
        COLOR_MAP.put(clazz, color);
        return callback;
    }
}
