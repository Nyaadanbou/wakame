package cc.mewcraft.wakame.gui

import cc.mewcraft.wakame.item.NekoStack
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper

internal fun NekoStack.toItemProvider(): ItemProvider {
    return ItemWrapper(itemStack)
}