package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.external.ComponentMap
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ComponentMapComponent(
    var componentMap: ComponentMap,
) : Component<ComponentMapComponent> {
    override fun type(): ComponentType<ComponentMapComponent> = ComponentMapComponent

    companion object : ComponentType<ComponentMapComponent>()
}
