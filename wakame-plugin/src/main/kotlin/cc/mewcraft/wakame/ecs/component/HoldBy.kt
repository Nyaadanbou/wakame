package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

// TODO 也许会变成在 BukkitPlayer FleksEntity 上的组件?
data class HoldBy(
    var nekoStack: NekoStack,
    var slot: ItemSlot
): Component<HoldBy> {
    companion object : ComponentType<HoldBy>()

    override fun type(): ComponentType<HoldBy> = HoldBy
}

fun HoldBy(pair: Pair<ItemSlot, NekoStack>?): HoldBy? {
    return pair?.let { HoldBy(it.second, it.first) }
}