package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import com.github.quillraven.fleks.Component

data class AtSlot(
    var slot: ItemSlot,
) : Component<AtSlot> {
    companion object : EComponentType<AtSlot>()

    override fun type(): EComponentType<AtSlot> = AtSlot
}