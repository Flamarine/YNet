package com.martmists.ynet

import com.martmists.ynet.block.CableBlock
import com.martmists.ynet.block.ConnectorBlock
import com.martmists.ynet.block.ControllerBlock
import com.martmists.ynet.util.YNetRegistry
import com.martmists.ynet.transfer.handler.ItemTransferHandler
import com.martmists.ynet.transfer.type.ItemType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import com.martmists.ynet.blockentity.ControllerBlockEntity
import com.martmists.ynet.screen.ControllerConfigScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.function.Supplier

object YNetMod : ModInitializer {
    // === BLOCKS ===
    val YNET_GROUP = FabricItemGroupBuilder.build(Identifier("ynet:items")) { ItemStack(Registry.ITEM[Identifier("ynet:controller")]) }
    val CABLE = register("cable", CableBlock(AbstractBlock.Settings.of(Material.METAL)))
    val CONNECTOR = register("connector", ConnectorBlock(AbstractBlock.Settings.of(Material.METAL)))
    val CONTROLLER = register("controller", ControllerBlock(AbstractBlock.Settings.of(Material.METAL)))

    // === BLOCK ENTITIES ===
    val CONTROLLER_BE = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            Identifier("ynet", "controller"),
            BlockEntityType.Builder.create(Supplier(::ControllerBlockEntity), CONTROLLER).build(null)
    )

    // === SCREEN HANDLERS ===
    val CONTROLLER_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(Identifier("ynet:controller")) { sid, inv, buf ->
        ControllerConfigScreenHandler(sid, inv.player, buf.readBlockPos())
    }

    private fun <T: Block> register(id: String, block: T) : T {
        Registry.register(Registry.BLOCK, Identifier("ynet", id), block)
        Registry.register(Registry.ITEM, Identifier("ynet", id), BlockItem(block, Item.Settings().group(YNET_GROUP)))
        return block
    }

    internal fun shouldConnect(world: World, pos: BlockPos): Boolean {
        return world.getBlockState(pos).block === CONTROLLER || YNetRegistry.getTypes().any {
            it.appliesTo(world, pos)
        }
    }

    internal fun shouldConnectCable(world: World, pos: BlockPos): Boolean {
        val b = world.getBlockState(pos).block
        return b === CABLE || b === CONNECTOR
    }

    override fun onInitialize() {
        FabricLoader.getInstance().getEntrypoints("ynet:support", Runnable::class.java).forEach {
            try {
                it.run()
            } catch (e: Exception) {
                println("An error occurred during ynet:support entrypoint $it")
                e.printStackTrace()
            }
        }
    }
}