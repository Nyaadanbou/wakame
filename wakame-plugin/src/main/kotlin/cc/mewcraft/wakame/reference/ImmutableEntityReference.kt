package cc.mewcraft.wakame.reference

import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity

class ImmutableEntityReference(
    override val name: String,
    override val keySet: Set<Key>,
) : EntityReference {
    override fun contains(entity: Entity): Boolean {
        TODO("Not yet implemented")
    }

    override fun contains(key: Key): Boolean {
        return key in keySet
    }
}