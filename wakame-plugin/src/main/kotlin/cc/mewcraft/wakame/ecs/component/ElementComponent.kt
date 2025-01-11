package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.core.RegistryEntry
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.element.ElementType
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ElementComponent(
    var element: RegistryEntry<ElementType> = KoishRegistries.ELEMENT.getDefaultEntry(),
) : Component<ElementComponent> {
    override fun type(): ComponentType<ElementComponent> = ElementComponent

    companion object : ComponentType<ElementComponent>()
}