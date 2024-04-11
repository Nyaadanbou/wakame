package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.registry.SkillInstanceRegistry
import cc.mewcraft.wakame.registry.SkillTypeRegistry
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun skillModule(): Module = module {

    singleOf(::SkillEventHandler)
    single { SkillInstanceRegistry } bind Initializable::class
    single { SkillTypeRegistry } bind Initializable::class
}