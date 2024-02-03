package cc.mewcraft.wakame.kizami

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun kizamiModule(): Module = module {
    singleOf(::KizamiSerializer)
}