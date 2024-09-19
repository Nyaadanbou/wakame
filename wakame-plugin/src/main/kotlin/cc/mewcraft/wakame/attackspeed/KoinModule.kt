package cc.mewcraft.wakame.attackspeed

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun attackSpeedModule(): Module = module {
    singleOf(::AttackSpeedEventHandler)
}