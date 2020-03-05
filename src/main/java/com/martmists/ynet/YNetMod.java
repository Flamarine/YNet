package com.martmists.ynet;

import com.martmists.ynet.api.BaseProvider;
import com.martmists.ynet.api.EnergyProvider;
import com.martmists.ynet.api.ItemProvider;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import com.martmists.ynet.blocks.CableBlock;
import com.martmists.ynet.blocks.ConnectorBlock;
import com.martmists.ynet.blocks.ControllerBlock;
import com.martmists.ynet.event.ProviderTickCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

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
    public static BlockEntityType<ControllerBlockEntity> CONTROLLER_BE = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            new Identifier("ynet", "controller"),
            BlockEntityType.Builder.create(ControllerBlockEntity::new, CONTROLLER).build(null)
    );

    public static Map<Class<? extends BaseProvider>, ProviderTickCallback<? extends BaseProvider>> PROVIDERS = new HashMap<>();
    public static Map<Class<? extends BaseProvider>, String> PROVIDER_NAMES = new HashMap<>();

    @Override
    public void onInitialize() {
        System.out.println("YNet loaded!");
        register("ynet:item", ItemProvider.class, (ItemProvider[] listeners, ControllerBlockEntity be) -> {
            // TODO:
            // - Get matching channels from ControllerBlockEntity
            // - Find a way to route all items (+ filters?
            return ActionResult.SUCCESS;
        });
        register("ynet:energy", EnergyProvider.class, (EnergyProvider[] listeners, ControllerBlockEntity be) -> {
            // TODO:
            // - Get matching channels from ControllerBlockEntity
            // - Find a way to route all energy
            return ActionResult.SUCCESS;
        });
        // TODO:
        // - Add support for configuring redstone signals on connectors
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

    static <T extends BaseProvider> ProviderTickCallback<T> register(String name, Class<T> clazz, ProviderTickCallback<T> callback) {
        PROVIDER_NAMES.put(clazz, name);
        PROVIDERS.put(clazz, callback);
        return callback;
    }
}
