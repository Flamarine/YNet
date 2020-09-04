package com.martmists.ynet.screen

import com.github.vini2003.blade.client.handler.BaseHandledScreen
import com.github.vini2003.blade.common.handler.BaseScreenHandler
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.TranslatableText

class ControllerConfigScreen(handler: BaseScreenHandler, inventory: PlayerInventory) : BaseHandledScreen<ControllerConfigScreenHandler>(handler, inventory, TranslatableText("ynet.ui.controller")) {
    override fun drawForeground(matrices: MatrixStack?, mouseX: Int, mouseY: Int) {

    }
}