package cc.mewcraft.wakame.world.entity

import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity

/**
 * A simple data structure holding a set of entity types.
 */
interface EntityTypeHolder {
    /**
     * The keys of the objects in this holder.
     */
    val keySet: Set<Key>

    /**
     * Returns `true` if the key of the [obj] is in the [keySet].
     *
     * @param obj the key of the object to be checked
     */
    fun contains(obj: Entity): Boolean

    /**
     * Returns `true` if the [key] is in the [keySet].
     *
     * @param key the key to be checked
     */
    fun contains(key: Key): Boolean
}