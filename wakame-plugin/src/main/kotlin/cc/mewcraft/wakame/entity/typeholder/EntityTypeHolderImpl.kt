package cc.mewcraft.wakame.entity.typeholder

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.world.entity.EntityKeyLookup
import cc.mewcraft.wakame.world.entity.EntityTypeHolder
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity

internal data class EntityTypeHolderImpl(
    override val keySet: Set<Key>,
) : EntityTypeHolder {
    private val entityKeyLookup: EntityKeyLookup by Injector.inject()

    override fun contains(obj: Entity): Boolean {
        return entityKeyLookup.get(obj) in keySet
    }

    override fun contains(key: Key): Boolean {
        return key in keySet
    }
}
