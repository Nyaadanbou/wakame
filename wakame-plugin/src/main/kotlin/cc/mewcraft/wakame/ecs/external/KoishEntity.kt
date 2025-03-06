package cc.mewcraft.wakame.ecs.external

import cc.mewcraft.wakame.ecs.ECS
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.UniqueId

class KoishEntity(
    private val entity: Entity,
) {

    internal fun invalidate() {
        ECS.removeEntity(entity)
    }

    internal inline operator fun <reified T : Component<out Any>> get(type: ComponentType<T>): T = with(ECS.world()) {
        return entity[type]
    }

    internal inline fun <reified T : Component<out Any>> getOrNull(type: ComponentType<T>): T? = with(ECS.world()) {
        return entity.getOrNull(type)
    }

    operator fun contains(uniqueId: UniqueId<out Any>): Boolean = with(ECS.world()) {
        return entity.contains(uniqueId)
    }

    infix fun has(type: UniqueId<*>): Boolean = with(ECS.world()) {
        return entity.has(type)
    }

    internal inline operator fun <reified T : Component<T>> plusAssign(component: T) {
        ECS.editEntity(entity) {
            it += component
        }
    }

    internal inline operator fun <reified T : Component<T>> minusAssign(type: ComponentType<T>) {
        ECS.editEntity(entity) {
            it -= type
        }
    }

    operator fun plusAssign(tag: EntityTags) {
        ECS.editEntity(entity) {
            it += tag
        }
    }

    operator fun minusAssign(tag: EntityTags) {
        ECS.editEntity(entity) {
            it -= tag
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KoishEntity) return false
        if (entity != other.entity) return false
        return true
    }

    override fun hashCode(): Int {
        return entity.hashCode()
    }

    override fun toString(): String {
        return entity.toString()
    }

}
