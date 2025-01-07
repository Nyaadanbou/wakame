package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.core.Holder
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registries.KoishRegistries
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ElementComponent(
    var element: Holder<Element> = KoishRegistries.ELEMENT.defaultValue,
) : Component<ElementComponent> {
    override fun type(): ComponentType<ElementComponent> = ElementComponent

    companion object : ComponentType<ElementComponent>()
}