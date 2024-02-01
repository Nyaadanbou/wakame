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
    protected val cacheMap: MutableMap<UUID, AttributeMap> = HashMap()

    /**
     * Gets the [AttributeMap] for the entity specified by [uuid].
     */
    abstract fun getAttributeMap(uuid: UUID): AttributeMap

    /**
     * Gets the [AttributeMap] for the [entity].
     */
    fun getAttributeMap(entity: T): AttributeMap {
        return getAttributeMap(entity.uniqueId)
    }

    /**
     * Removes the [AttributeMap] from memory for the [entity].
     */
    fun removeAttributeMap(entity: T) {
        cacheMap.remove(entity.uniqueId)
    }
}

/**
 * Provides the access to the [AttributeMap] of all online players.
 *
 * **Not thread-safe.**
 */
class PlayerAttributeAccessor : AttributeAccessor<Player>() {
    override fun getAttributeMap(uuid: UUID): AttributeMap {
        return cacheMap.computeIfAbsent(uuid) {
            AttributeMap(DefaultAttributes.getSupplier(EntityType.PLAYER))
        }
    }
}

/**
 * Provides the access to [AttributeMap] of all non-player entities.
 *
 * **Not thread-safe.**
 */

class EntityAttributeAccessor : AttributeAccessor<Entity>() {
    override fun getAttributeMap(uuid: UUID): AttributeMap {
        TODO("Read the NBT on the entity. Use cache if feasible")
    }
}