package cc.mewcraft.wakame.skill.tick

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun skillTickModule(): Module = module {
    singleOf(::SkillTickerListener)
}