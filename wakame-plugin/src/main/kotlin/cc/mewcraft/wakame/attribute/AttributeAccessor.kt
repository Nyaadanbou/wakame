package cc.mewcraft.wakame.attribute

import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.*

/**
 * Provides the access to [AttributeMap] of various objects.
 *
 * **Not thread-safe.**
 */
sealed class AttributeAccessor<T : Entity> {
    protected val cacheMap: MutableMap<UUID, AttributeMap> = hashMapOf()

    /**
     * Gets the [AttributeMap] for the [entity].
     */
    abstract fun getAttributeMap(entity: T): AttributeMap

    /**
     * Removes the [AttributeMap] from memory for the [entity].
     */
    fun removeAttributeMap(entity: T) {
        cacheMap[entity.uniqueId]?.clearAllModifiers()
        cacheMap.remove(entity.uniqueId)
    }
}

/**
 * Provides the access to the [PlayerAttributeMap] of all online players.
 *
 * **Not thread-safe.**
 */
class PlayerAttributeAccessor : AttributeAccessor<Player>() {
    override fun getAttributeMap(entity: Player): AttributeMap {
        return cacheMap.computeIfAbsent(entity.uniqueId) {
            PlayerAttributeMap(DefaultAttributes.getSupplier(EntityType.PLAYER), entity)
        }
    }
}

/**
 * Provides the access to [AttributeMap] of all non-player entities.
 *
 * **Not thread-safe.**
 */
class EntityAttributeAccessor : AttributeAccessor<Entity>() {
    override fun getAttributeMap(entity: Entity): PlayerAttributeMap {
        TODO("Read the NBT on the entity. Use cache if feasible")
    }
}