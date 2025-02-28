package cc.mewcraft.wakame.entity

import cc.mewcraft.wakame.entity.MinecraftEntityKeyLookupDictionary.get
import cc.mewcraft.wakame.world.entity.EntityKeyLookup
import net.kyori.adventure.key.Key
import org.bukkit.Registry
import org.bukkit.entity.Entity

internal class EntityKeyLookupImpl(
    private val lookupList: List<EntityKeyLookup.Dictionary>,
) : EntityKeyLookup {
    override fun get(entity: Entity): Key {
        for (lookup in lookupList) {
            val key = lookup.get(entity)
            if (key != null) {
                return key // return first non-null key
            }
        }

        // fallback
        return MinecraftEntityKeyLookupDictionary.get(entity)
    }

    override fun validate(key: Key): Boolean {
        return lookupList.any { it.validate(key) }
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
private object MinecraftEntityKeyLookupDictionary : EntityKeyLookup.Dictionary {
    override fun get(entity: Entity): Key =
        entity.type.key

    override fun validate(key: Key): Boolean =
        Registry.ENTITY_TYPE.get(key) != null
}