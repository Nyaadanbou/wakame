package cc.mewcraft.wakame.item.components.cells.cores.skill

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

internal fun coreSkillModule(): Module = module {
    single<SkillCoreBootstrap> { SkillCoreBootstrap } withOptions { bind<Initializable>() }
    single<SkillLineKeyFactory> { SkillLineKeyFactory(get()) }
}