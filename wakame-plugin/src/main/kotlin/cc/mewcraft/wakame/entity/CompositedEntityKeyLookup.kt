package cc.mewcraft.wakame.entity

import cc.mewcraft.wakame.annotation.InternalApi
import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.createScope
import org.koin.core.scope.Scope

@InternalApi
class CompositedEntityKeyLookup(
    private val lookupList: List<EntityKeyLookup>,
) : KoinScopeComponent, EntityKeyLookup {

    override val scope: Scope by lazy { createScope(this) }

    init {
        scope.close()
    }

    override fun getKey(entity: Entity): Key? {
        for (lookup in lookupList) {
            val key = lookup.getKey(entity)
            if (key != null) {
                return key // return first non-null key
            }
        }
        return null
    }

}