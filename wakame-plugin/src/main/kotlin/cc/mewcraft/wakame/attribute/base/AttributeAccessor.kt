package cc.mewcraft.wakame.attribute.base

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
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
    protected val cacheMap: Cache<UUID, AttributeMap> = CacheBuilder.newBuilder()
        .weakValues()
        .build()

    /**
     * Gets the [AttributeMap] for the [entity].
     */
    abstract fun getAttributeMap(entity: T): AttributeMap

    /**
     * Removes the [AttributeMap] from memory for the [entity].
     */
    fun removeAttributeMap(entity: T) {
        cacheMap.getIfPresent(entity.uniqueId)?.clearAllModifiers()
        cacheMap.invalidate(entity.uniqueId)
    }
}

/**
 * Provides the access to the [AttributeMap] of all online players.
 *
 * **Not thread-safe.**
 */
class PlayerAttributeAccessor : AttributeAccessor<Player>() {
    override fun getAttributeMap(entity: Player): AttributeMap {
        return cacheMap.get(entity.uniqueId) {
            AttributeMap(DefaultAttributes.getSupplier(EntityType.PLAYER), entity)
        }
    }
}

/**
 * Provides the access to [AttributeMap] of all non-player entities.
 *
 * **Not thread-safe.**
 */
class EntityAttributeAccessor : AttributeAccessor<Entity>() {
    override fun getAttributeMap(entity: Entity): AttributeMap {
        TODO("Read the NBT on the entity. Use cache if feasible")
    }
}