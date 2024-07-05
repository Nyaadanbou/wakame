package cc.mewcraft.wakame.entity

import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity

/**
 * **Internal Use Only.**
 *
 * This interface is to form the implementation of [EntityKeyLookup] which
 * consists of zero or more instances of [EntityKeyLookupPart].
 */
interface EntityKeyLookupPart {

    /**
     * Gets the key of [entity]. If the implementation can't find a proper key
     * for the [entity], this function should return `null`.
     *
     * @param entity the entity from which you look up the key
     * @return the key of the entity
     */
    fun get(entity: Entity): Key?

    /**
     * 检查 [key] 是否有效 (对应的生物种类存在).
     */
    fun validate(key: Key): Boolean

}