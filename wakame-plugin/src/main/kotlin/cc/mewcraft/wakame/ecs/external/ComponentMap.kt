package cc.mewcraft.wakame.ecs.external

import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Snapshot
import com.google.common.collect.ImmutableTable

data class ComponentMap(
    /**
     * 存放所有外部组件的表.
     *
     * R - [com.github.quillraven.fleks.Entity] 标记的 [cc.mewcraft.wakame.ecs.component.IdentifierComponent.id].
     * C - [ComponentType], 所有 [ComponentType] 在各个 [Component] 内静态存储.
     * V - [Component] 实例.
     */
    val componentTable: ImmutableTable<String, ComponentType<out Any>, Component<out Any>>,
) {
    operator fun <T : Component<out Any>> get(identifier: String, type: ComponentType<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return componentTable.get(identifier, type) as? T
    }
}

/* ECS extensions */

fun ComponentMap(snapshot: Snapshot): ComponentMap {
    val components = snapshot.components
    val identifierComponent = snapshot.components.find { it.type() == IdentifierComponent } as? IdentifierComponent ?: throw error("Entity doesn't have IdentifierComponent")
    val immutableTable = ImmutableTable.builder<String, ComponentType<out Any>, Component<out Any>>()

    for (component in components) {
        immutableTable.put(identifierComponent.id, component.type(), component)
    }

    return ComponentMap(immutableTable.build())
}
