package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry.ElementRegistry
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ElementComponent(
    var element: Element = ElementRegistry.DEFAULT
) : Component<ElementComponent> {
    override fun type(): ComponentType<ElementComponent> = ElementComponent

    companion object : ComponentType<ElementComponent>()
}