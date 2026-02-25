package cc.mewcraft.wakame.item.network

import cc.mewcraft.wakame.item.display.ShowItemRenderer
import net.kyori.adventure.text.Component

object ShowItemRendererImpl : ShowItemRenderer {
    override fun render(component: Component): Component {
        return ItemStackRenderer.renderShowItem(null, component)
    }
}