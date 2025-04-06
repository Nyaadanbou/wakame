package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.item2.config.property.impl.ItemSlot
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class AtSlot(
    var slot: ItemSlot,
) : Component<AtSlot> {
    companion object : ComponentType<AtSlot>()

    override fun type(): ComponentType<AtSlot> = AtSlot
}