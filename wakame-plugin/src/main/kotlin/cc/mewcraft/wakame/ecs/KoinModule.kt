package cc.mewcraft.wakame.ecs

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun ecsModule(): Module = module {
    singleOf(::WakameWorld)
    singleOf(::EcsListener)
}