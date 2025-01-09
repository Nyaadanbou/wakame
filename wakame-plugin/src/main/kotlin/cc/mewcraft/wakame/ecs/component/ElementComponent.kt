package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.core.RegistryEntry
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.element.Element
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ElementComponent(
    var element: RegistryEntry<Element> = KoishRegistries.ELEMENT.getDefaultEntry(),
) : Component<ElementComponent> {
    override fun type(): ComponentType<ElementComponent> = ElementComponent

    companion object : ComponentType<ElementComponent>()
}