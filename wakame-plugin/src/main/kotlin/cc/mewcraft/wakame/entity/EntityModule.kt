@file:OptIn(InternalApi::class)

package cc.mewcraft.wakame.entity

import cc.mewcraft.wakame.annotation.InternalApi
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.scopedOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun entityModule(): Module = module {
    scope<CompositedEntityKeyLookup> {
        scopedOf(::VanillaEntityKeyLookup)
        scopedOf(::MythicMobsEntityKeyLookup)
    }

    singleOf(::CompositedEntityKeyLookup) { bind<EntityKeyLookup>() }
}