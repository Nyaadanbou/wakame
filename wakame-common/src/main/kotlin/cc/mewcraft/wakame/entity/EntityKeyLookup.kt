package cc.mewcraft.wakame.entity

import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity

interface EntityKeyLookup {

    /**
     * Gets the key of [entity]. If the implementation can't find a proper key
     * for the [entity], this function should return `null`.
     *
     * @param entity the entity from which you look up the key
     * @return the key of the entity
     */
    fun getKey(entity: Entity): Key?

}