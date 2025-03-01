package cc.mewcraft.wakame.ecs.external

import cc.mewcraft.wakame.ecs.WakameWorld
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityTag
import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.UniqueId

// TODO: 改名成 ComponentBridge
@JvmInline
value class ComponentMap(
    val entity: Entity,
) {
    /**
     * 存放所有外部组件的键值对.
     *
     * K - [ComponentType], 所有 [ComponentType] 在各个 [Component] 内静态存储.
     * V - [Component] 实例.
     */
    val componentsMap: Map<ComponentType<out Any>, Component<out Any>>
        get() = WakameWorld.world().snapshotOf(entity).components.associate { it.type() to it }

    /**
     * [EntityTag] 列表.
     */
    val tags: List<UniqueId<out Any>>
        get() = WakameWorld.world().snapshotOf(entity).tags

    internal inline operator fun <reified T : Component<out Any>> get(type: ComponentType<T>): T? = with(WakameWorld.world()) {
        return entity.getOrNull(type)
    }

    operator fun contains(uniqueId: UniqueId<out Any>): Boolean = with(WakameWorld.world()) {
        return entity.contains(uniqueId)
    }

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
}
