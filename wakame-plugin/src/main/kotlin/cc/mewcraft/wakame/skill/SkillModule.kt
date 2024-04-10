package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.condition.DurabilityCondition
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun skillModule(): Module = module {

    singleOf(::SkillEventHandler)
    single { SkillRegistry } bind Initializable::class
    single { SkillTemplates } bind Initializable::class

    single { DurabilityCondition }
}