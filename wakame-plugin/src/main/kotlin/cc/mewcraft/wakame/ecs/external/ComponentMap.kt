package cc.mewcraft.wakame.ecs.external

import cc.mewcraft.wakame.ecs.WakameWorld
import com.github.quillraven.fleks.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ComponentMap(
    val entity: Entity,

    /**
     * 存放所有外部组件的键值对.
     *
     * K - [ComponentType], 所有 [ComponentType] 在各个 [Component] 内静态存储.
     * V - [Component] 实例.
     */
    val componentsMap: Map<ComponentType<out Any>, Component<out Any>> = hashMapOf(),

    /**
     * [EntityTag] 列表.
     */
    val tags: List<UniqueId<out Any>>,
) {
    companion object : KoinComponent {
        private val wakameWorld: WakameWorld by inject()
    }

    constructor(
        world: World,
        entity: Entity,
    ) : this(entity, world.snapshotOf(entity).components.associate { it.type() to it }.toMap(), world.snapshotOf(entity).tags.toList())

    operator fun <T : Component<out Any>> get(type: ComponentType<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return componentsMap[type] as? T
    }

    operator fun contains(type: ComponentType<out Any>): Boolean = componentsMap.containsKey(type)

    operator fun contains(tag: EntityTags): Boolean = tags.contains(tag)

    internal inline operator fun <reified T : Component<T>> plusAssign(component: T) {
        wakameWorld.editEntity(entity) {
            it += component
        }
    }

    internal inline operator fun <reified T : Component<T>> minusAssign(type: ComponentType<T>) {
        wakameWorld.editEntity(entity) {
            it -= type
        }
    }

    operator fun plusAssign(tag: EntityTags) {
        wakameWorld.editEntity(entity) {
            it += tag
        }
    }

    operator fun minusAssign(tag: EntityTags) {
        wakameWorld.editEntity(entity) {
            it -= tag
        }
    }
}
