package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ElementComponent(
    var element: RegistryEntry<out Element> = KoishRegistries.ELEMENT.getDefaultEntry(),
) : Component<ElementComponent> {
    override fun type(): ComponentType<ElementComponent> = ElementComponent

    companion object : ComponentType<ElementComponent>()
}