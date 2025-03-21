package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class HoldBy(
    var slot: ItemSlot,
    var nekoStack: NekoStack,
): Component<HoldBy> {
    companion object : ComponentType<HoldBy>()

    override fun type(): ComponentType<HoldBy> = HoldBy
}

fun HoldBy(pair: Pair<ItemSlot, NekoStack>?): HoldBy? {
    return pair?.let { HoldBy(it.first, it.second) }
}