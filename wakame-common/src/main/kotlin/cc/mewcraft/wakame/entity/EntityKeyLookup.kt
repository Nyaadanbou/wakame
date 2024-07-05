package cc.mewcraft.wakame.entity

import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity

interface EntityKeyLookup {

    /**
     * Gets the key of [entity].
     *
     * This function always returns a non-null value. In the case where we
     * can't find a key for the entity, the vanilla key will be returned.
     *
     * @param entity the entity from which you look up the key
     * @return the key of the entity
     */
    fun get(entity: Entity): Key

    /**
     * 检查 [key] 是否有效 (对应的生物种类存在).
     */
    fun validate(key: Key): Boolean
}