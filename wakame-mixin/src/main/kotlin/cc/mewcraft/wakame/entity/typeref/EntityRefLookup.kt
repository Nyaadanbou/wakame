package cc.mewcraft.wakame.entity.typeref

import cc.mewcraft.wakame.entity.typeref.MinecraftEntityRefLookupDictionary.get
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import net.kyori.adventure.key.Key
import org.bukkit.Registry
import org.bukkit.entity.Entity

object EntityRefLookup {

    /**
     * Gets the key of [entity].
     *
     * This function always returns a non-null value. In the case where we
     * can't find a key for the entity, the vanilla key will be returned.
     *
     * @param entity the entity from which you look up the key
     * @return the key of the entity
     */
    fun get(entity: Entity): Key {
        for (lookup in BuiltInRegistries.ENTITY_REF_LOOKUP_DIR) {
            val key = lookup.get(entity)
            if (key != null) {
                return key // return first non-null key
            }
        }

        // fallback
        return MinecraftEntityRefLookupDictionary.get(entity)
    }

    /**
     * 检查 [key] 是否有效 (对应的生物种类存在).
     */
    fun validate(key: Key): Boolean {
        return BuiltInRegistries.ENTITY_REF_LOOKUP_DIR.any { it.validate(key) }
    }

    /**
     * This interface is to form the implementation of [EntityRefLookup]
     * which consists of zero or more instances of [Dictionary]s.
     */
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

/**
 * An entity key lookup for Minecraft vanilla.
 *
 * Note that the [get] never returns `null` as all entities, no matter
 * whether they are strictly-vanilla or not, **are** all vanilla entities.
 * So, you should call other implementation first, and this implementation
 * should always be the last to be called.
 */
private object MinecraftEntityRefLookupDictionary : EntityRefLookup.Dictionary {
    override fun get(entity: Entity): Key =
        entity.type.key

    override fun validate(key: Key): Boolean =
        Registry.ENTITY_TYPE.get(key) != null
}