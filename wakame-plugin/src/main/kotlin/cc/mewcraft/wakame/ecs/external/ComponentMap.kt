package cc.mewcraft.wakame.ecs.external

import cc.mewcraft.wakame.ecs.WakameWorld
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityTag
import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.UniqueId
import com.github.quillraven.fleks.World

class ComponentMap internal constructor(
    world: World,
    val entity: Entity,
) {

    /**
     * 存放所有外部组件的键值对.
     *
     * K - [ComponentType], 所有 [ComponentType] 在各个 [Component] 内静态存储.
     * V - [Component] 实例.
     */
    val componentsMap: Map<ComponentType<out Any>, Component<out Any>> = world.snapshotOf(entity).components.associate { it.type() to it }.toMap()

    /**
     * [EntityTag] 列表.
     */
    val tags: List<UniqueId<out Any>> = world.snapshotOf(entity).tags.toList()

    operator fun <T : Component<out Any>> get(type: ComponentType<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return componentsMap[type] as? T
    }

    operator fun contains(type: ComponentType<out Any>): Boolean = componentsMap.containsKey(type)

    operator fun contains(tag: EntityTags): Boolean = tags.contains(tag)

    internal inline operator fun <reified T : Component<T>> plusAssign(component: T) {
        WakameWorld.editEntity(entity) {
            it += component
        }
    }

    internal inline operator fun <reified T : Component<T>> minusAssign(type: ComponentType<T>) {
        WakameWorld.editEntity(entity) {
            it -= type
        }
    }

    operator fun plusAssign(tag: EntityTags) {
        WakameWorld.editEntity(entity) {
            it += tag
        }
    }

    operator fun minusAssign(tag: EntityTags) {
        WakameWorld.editEntity(entity) {
            it -= tag
        }
    }

    override fun toString(): String {
        return "ComponentMap(entity=$entity, componentsMap=$componentsMap, tags=$tags)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ComponentMap

        return entity == other.entity
    }

    override fun hashCode(): Int {
        return entity.hashCode()
    }

}
