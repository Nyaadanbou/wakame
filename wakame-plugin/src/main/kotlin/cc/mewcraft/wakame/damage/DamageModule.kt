package cc.mewcraft.wakame.damage

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun damageModule(): Module = module {
    singleOf(::DamageListener)
}