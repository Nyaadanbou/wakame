package cc.mewcraft.wakame.display2

import cc.mewcraft.wakame.item.NekoStack

interface TooltipNameTransformer {
    fun transform(nyaaStack: NekoStack): NekoStack
}

interface TooltipLoreTransformer {
    fun transform(nyaaStack: NekoStack): NekoStack
}
