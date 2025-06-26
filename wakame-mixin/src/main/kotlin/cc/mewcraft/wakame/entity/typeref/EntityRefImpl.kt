package cc.mewcraft.wakame.entity.typeref

import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity

data class EntityRefImpl(
    override val keySet: Set<Key>,
) : EntityRef {
    override fun contains(obj: Entity): Boolean {
        return EntityRefLookup.get(obj) in keySet
    }

    override fun contains(key: Key): Boolean {
        return key in keySet
    }
}
