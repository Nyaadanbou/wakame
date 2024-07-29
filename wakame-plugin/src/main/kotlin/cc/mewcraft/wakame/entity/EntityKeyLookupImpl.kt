package cc.mewcraft.wakame.entity

import cc.mewcraft.wakame.entity.MinecraftEntityKeyLookup.get
import cc.mewcraft.wakame.util.toNamespacedKey
import net.kyori.adventure.key.Key
import org.bukkit.Registry
import org.bukkit.entity.Entity
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.createScope
import org.koin.core.scope.Scope

internal class EntityKeyLookupImpl(
    private val lookupList: List<EntityKeyLookupPart>,
) : KoinScopeComponent, EntityKeyLookup {

    override val scope: Scope by lazy { createScope(this) }

    init {
        scope.close()
    }

    override fun get(entity: Entity): Key {
        for (lookup in lookupList) {
            val key = lookup.get(entity)
            if (key != null) {
                return key // return first non-null key
            }
        }

        // fallback
        return MinecraftEntityKeyLookup.get(entity)
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
private object MinecraftEntityKeyLookup : EntityKeyLookupPart {

    override fun get(entity: Entity): Key {
        return entity.type.key
    }

    override fun validate(key: Key): Boolean {
        return Registry.ENTITY_TYPE.get(key.toNamespacedKey()) != null
    }

}