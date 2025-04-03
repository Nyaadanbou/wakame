package cc.mewcraft.wakame.entity.typeref

import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import org.jetbrains.annotations.ApiStatus

interface EntityRefLookup {

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

    /**
     * This interface is to form the implementation of [EntityRefLookup]
     * which consists of zero or more instances of [Dictionary]s.
     */
    @ApiStatus.Internal
    interface Dictionary {

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
}