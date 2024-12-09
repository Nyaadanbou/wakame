package cc.mewcraft.wakame.ecs.external

import com.github.quillraven.fleks.*

data class ComponentMap(
    val world: World,
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

    constructor(
        world: World,
        entity: Entity,
    ) : this(world, entity, world.snapshotOf(entity).components.associate { it.type() to it }.toMap(), world.snapshotOf(entity).tags.toList())

    operator fun <T : Component<out Any>> get(type: ComponentType<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return componentsMap[type] as? T
    }

    inline operator fun <reified T : Component<T>> plusAssign(component: T) {
        with(world) {
            entity.configure {
                it += component
            }
        }
    }

    operator fun plusAssign(tag: EntityTags) {
        with(world) {
            entity.configure {
                it += tag
            }
        }
    }
}
