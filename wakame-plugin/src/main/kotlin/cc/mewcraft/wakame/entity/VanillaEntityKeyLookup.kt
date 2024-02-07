package cc.mewcraft.wakame.entity

import cc.mewcraft.wakame.annotation.InternalApi
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity

/**
 * An entity key lookup for Minecraft vanilla.
 *
 * Note that the [getKey] never returns `null` as all entities, no matter
 * whether they are strictly-vanilla or not, **are** all vanilla entities.
 * So, you should call other implementation first, and this implementation
 * should always be the last to be called.
 */
@InternalApi
class VanillaEntityKeyLookup : EntityKeyLookup {

    override fun getKey(entity: Entity): Key {
        return entity.type.key
    }

}