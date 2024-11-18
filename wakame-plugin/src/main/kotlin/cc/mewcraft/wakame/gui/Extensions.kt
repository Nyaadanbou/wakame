package cc.mewcraft.wakame.gui

import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.simple.SimpleItemRendererContext
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper

internal fun MenuLayout.getFixedIconItemProvider(id: String): ItemProvider {
    val nekoStack = this.getIcon(id)
    ItemRenderers.SIMPLE.render(nekoStack, SimpleItemRendererContext())
    return ItemWrapper(nekoStack.itemStack)
}