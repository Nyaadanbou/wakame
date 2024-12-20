package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class HoldBy(
    var nekoStack: NekoStack,
    var slot: ItemSlot
): Component<HoldBy> {
    override fun type(): ComponentType<HoldBy> = HoldBy

    companion object : ComponentType<HoldBy>()
}