package cc.mewcraft.wakame.ecs.bridge

import cc.mewcraft.wakame.ecs.Fleks
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.UniqueId

class KoishEntity(
    @PublishedApi
    internal val entity: EEntity,
) {

    fun unwrap(): EEntity =
        entity

    fun remove() =
        with(Fleks.INSTANCE.world) { entity.remove() }

    inline operator fun <reified T : Component<out Any>> get(type: ComponentType<T>): T =
        with(Fleks.INSTANCE.world) { entity[type] }

    inline fun <reified T : Component<out Any>> getOrNull(type: ComponentType<T>): T? =
        with(Fleks.INSTANCE.world) { entity.getOrNull(type) }

    operator fun contains(uniqueId: UniqueId<out Any>): Boolean =
        with(Fleks.INSTANCE.world) { entity.contains(uniqueId) }

    infix fun has(type: UniqueId<*>): Boolean =
        with(Fleks.INSTANCE.world) { entity.has(type) }

    inline operator fun <reified T : Component<T>> plusAssign(component: T) {
        Fleks.INSTANCE.editEntity(entity) { it += component }
    }

    inline operator fun <reified T : Component<T>> minusAssign(type: ComponentType<T>) {
        Fleks.INSTANCE.editEntity(entity) { it -= type }
    }

    operator fun plusAssign(tag: EntityTags) {
        Fleks.INSTANCE.editEntity(entity) { it += tag }
    }

    operator fun minusAssign(tag: EntityTags) {
        Fleks.INSTANCE.editEntity(entity) { it -= tag }
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
