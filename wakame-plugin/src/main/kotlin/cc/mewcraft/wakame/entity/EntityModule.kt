package cc.mewcraft.wakame.entity

import cc.mewcraft.wakame.Koish
import cc.mewcraft.wakame.world.entity.EntityKeyLookup
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun entityModule(): Module = module {
    single<EntityKeyLookup> {
        fun registerImplementation(
            requiredPlugin: String,
            implementations: MutableList<EntityKeyLookup.Dictionary>,
            implementationCreator: () -> EntityKeyLookup.Dictionary,
        ) {
            if (Koish.isPluginPresent(requiredPlugin)) {
                implementations.add(implementationCreator())
            }
        }

        EntityKeyLookupImpl(buildList {
            // 目前的所有实现暂时不需要获取 MythicMobs 的怪物的 id, 等之后需要的时候再把这个注释给去掉.
            // registerImplementation("MythicMobs", this, ::MythicMobsEntityKeyLookup)
        })
    }
}