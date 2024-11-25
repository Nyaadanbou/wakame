package cc.mewcraft.wakame.skill2

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun skill2Module(): Module = module {
    singleOf(::SkillManager)

    singleOf(::SkillListener)
}