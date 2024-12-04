package cc.mewcraft.wakame.ecs.external

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.EntityTag
import com.github.quillraven.fleks.Snapshot
import com.github.quillraven.fleks.UniqueId

data class ComponentMap(
    /**
     * 存放所有外部组件的键值对.
     *
     * K - [ComponentType], 所有 [ComponentType] 在各个 [Component] 内静态存储.
     * V - [Component] 实例.
     */
    val componentsMap: MutableMap<ComponentType<out Any>, Component<out Any>> = hashMapOf(),

    /**
     * [EntityTag] 列表.
     */
    val tags: MutableList<UniqueId<out Any>>
) {

    constructor(snapshot: Snapshot) : this(snapshot.components.associate { it.type() to it }.toMutableMap(), snapshot.tags.toMutableList())

    operator fun <T : Component<out Any>> get(type: ComponentType<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return componentsMap[type] as? T
    }

    operator fun <T : Component<out Any>> set(type: ComponentType<T>, value: Component<T>) {
        componentsMap.put(type, value)
    }

    fun toSnapshot(): Snapshot {
        return Snapshot(componentsMap.values.toList(), tags)
    }
}
