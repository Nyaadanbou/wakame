package cc.mewcraft.wakame.entity.typeref

import cc.mewcraft.wakame.entity.typeref.MinecraftEntityRefLookupDictionary.get
import net.kyori.adventure.key.Key
import org.bukkit.Registry
import org.bukkit.entity.Entity

class EntityRefLookupImpl(
    private val lookupList: List<EntityRefLookup.Dictionary>,
) : EntityRefLookup {
    override fun get(entity: Entity): Key {
        for (lookup in lookupList) {
            val key = lookup.get(entity)
            if (key != null) {
                return key // return first non-null key
            }
        }

        // fallback
        return MinecraftEntityRefLookupDictionary.get(entity)
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
private object MinecraftEntityRefLookupDictionary : EntityRefLookup.Dictionary {
    override fun get(entity: Entity): Key =
        entity.type.key

    override fun validate(key: Key): Boolean =
        Registry.ENTITY_TYPE.get(key) != null
}