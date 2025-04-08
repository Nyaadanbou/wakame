package cc.mewcraft.wakame.element.component

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ElementComponent(
    val element: RegistryEntry<Element>,
) : Component<ElementComponent> {
    companion object : ComponentType<ElementComponent>()

    override fun type(): ComponentType<ElementComponent> = ElementComponent
}