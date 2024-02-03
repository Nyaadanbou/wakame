package cc.mewcraft.wakame.reference

import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity

/**
 * Just a simple data structure which holds a set of entity [keys][Key].
 */
interface EntityReference {
    /**
     * The unique name of this entity reference.
     */
    val name: String

    /**
     * The keys of entities.
     */
    val keySet: Set<Key>

    /**
     * Returns `true` if the key of the [entity] is in the [keySet].
     *
     * @param entity the key of the entity to be checked
     */
    fun contains(entity: Entity): Boolean

    /**
     * Returns `true` if the [key] is in the [keySet].
     *
     * @param key the key to be checked
     */
    fun contains(key: Key): Boolean
}