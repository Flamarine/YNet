package com.martmists.ynet.screen.util

import com.github.vini2003.blade.client.utilities.Drawings
import com.github.vini2003.blade.client.utilities.Instances
import com.github.vini2003.blade.common.widget.base.AbstractWidget
import com.github.vini2003.blade.common.widget.base.ItemWidget
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

class CustomTooltipItemWidget(val stack: ItemStack = ItemStack.EMPTY, val tooltip: Text) : AbstractWidget() {
    override fun getTooltip(): List<Text> {
        return if (stack.isEmpty) emptyList() else listOf(tooltip)
    }

    override fun drawWidget(matrices: MatrixStack, provider: VertexConsumerProvider) {
        if (hidden) return

        Drawings.getItemRenderer()?.renderInGui(stack, position.x.toInt(), position.y.toInt())
    }
}