package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.item.ItemSlot
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class InSlot(
    var slot: ItemSlot,
): Component<InSlot> {
    companion object : ComponentType<InSlot>()

    override fun type(): ComponentType<InSlot> = InSlot
}