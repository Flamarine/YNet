package com.martmists.ynet.util

import com.github.vini2003.blade.common.data.Color
import com.github.vini2003.blade.common.data.Position
import com.github.vini2003.blade.common.data.Size
import com.github.vini2003.blade.common.handler.BaseScreenHandler
import com.github.vini2003.blade.common.widget.WidgetCollection
import com.github.vini2003.blade.common.widget.base.*
import net.minecraft.block.Block
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text

object WidgetBuilder {
    fun build(s: BaseScreenHandler, callback: Builder<PanelWidget>.() -> Unit): PanelWidget {
        val b = Builder(PanelWidget().also {
            it.original = s
            it.handler = s
        })
        callback(b)
        return b.root
    }

    class Builder<T>(override val root: T) : SingleBuilder<T>(root) where T : AbstractWidget, T : WidgetCollection {
        private fun <O> buildInnerCollection(callback: Builder<O>.() -> Unit, base: O): O where O : AbstractWidget, O: WidgetCollection {
            base.parent = root
            base.position = Position.of(root)
            root.addWidget(base)
            val b = Builder(base)
            callback(b)
            return base
        }

        private fun <O : AbstractWidget> buildInner(callback: SingleBuilder<O>.() -> Unit, base: O): O {
            base.parent = root
            base.position = Position.of(root)
            root.addWidget(base)
            val b = SingleBuilder(base)
            callback(b)
            return base
        }

        fun panel(callback: Builder<PanelWidget>.() -> Unit) : PanelWidget {
            val p = PanelWidget()
            buildInnerCollection(callback, p)
            return p
        }

        fun button(onClick: (ButtonWidget) -> Unit) = button(onClick) { }
        fun button(onClick: (ButtonWidget) -> Unit, callback: SingleBuilder<ButtonWidget>.() -> Unit): ButtonWidget {
            val b = ButtonWidget(onClick)
            buildInner(callback, b)
            return b
        }
        fun button(color: Color, onClick: (ColoredButtonWidget) -> Unit) = button(color, onClick) { }
        fun button(color: Color, onClick: (ColoredButtonWidget) -> Unit, callback: SingleBuilder<ColoredButtonWidget>.() -> Unit): ColoredButtonWidget {
            val b = ColoredButtonWidget(onClick, color)
            buildInner(callback, b)
            return b
        }

        fun list(callback: Builder<ListWidget>.() -> Unit) : ListWidget {
            val l = ListWidget()
            buildInnerCollection(callback, l)
            return l
        }

        fun text(txt: Text) = text(txt) { }
        fun text(txt: Text, callback: SingleBuilder<TextWidget>.() -> Unit): TextWidget {
            val t = TextWidget()
            t.text = txt
            buildInner(callback, t)
            return t
        }

        fun icon(b: Block) = icon(b) { }
        fun icon(b: Block, callback: SingleBuilder<ItemWidget>.() -> Unit): ItemWidget {
            val i = ItemWidget()
            i.stack = ItemStack(b)
            buildInner(callback, i)
            return i
        }
    }

    open class SingleBuilder<T : AbstractWidget>(open val root: T) {
        fun position(pos: Position) {
            root.position = pos
        }

        fun relativePosition(x: Number, y: Number) = position(Position.of(root.parent!!, x, y))

        fun size(size: Size) {
            root.size = size
        }

        fun hidden(boolean: Boolean) {
            root.hidden = boolean
        }
    }
}
