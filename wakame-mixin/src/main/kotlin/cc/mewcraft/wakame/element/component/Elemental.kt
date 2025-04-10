package cc.mewcraft.wakame.element.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import com.github.quillraven.fleks.Component

data class Elemental(
    val element: RegistryEntry<Element>,
) : Component<Elemental> {
    companion object : EComponentType<Elemental>()

    override fun type(): EComponentType<Elemental> = Elemental
}