package com.martmists.ynet

import com.martmists.ynet.screen.ControllerConfigScreen
import com.martmists.ynet.screen.ControllerConfigScreenHandler
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

object YNetModClient : ClientModInitializer {
    val CONTROLLER_SCREEN = ScreenRegistry.register(YNetMod.CONTROLLER_SCREEN_HANDLER) { controllerConfigScreenHandler: ControllerConfigScreenHandler, playerInventory: PlayerInventory, text: Text ->
        return@register ControllerConfigScreen(controllerConfigScreenHandler, playerInventory)
    }

    override fun onInitializeClient() {

    }
}
