package cc.mewcraft.wakame.reference

import cc.mewcraft.wakame.entity.EntityKeyLookup
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal data class EntityReferenceImpl(
    override val name: String,
    override val keySet: Set<Key>,
) : KoinComponent, EntityReference {
    private val entityKeyLookup: EntityKeyLookup by inject()

    override fun contains(entity: Entity): Boolean {
        return entityKeyLookup.getKey(entity) in keySet
    }

    override fun contains(key: Key): Boolean {
        return key in keySet
    }
}