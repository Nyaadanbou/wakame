package cc.mewcraft.wakame.entity.typeref

import cc.mewcraft.wakame.Injector
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity

data class EntityRefImpl(
    override val keySet: Set<Key>,
) : EntityRef {
    private val entityRefLookup: EntityRefLookup by Injector.inject<EntityRefLookup>()

    override fun contains(obj: Entity): Boolean {
        return entityRefLookup.get(obj) in keySet
    }

    override fun contains(key: Key): Boolean {
        return key in keySet
    }
}
