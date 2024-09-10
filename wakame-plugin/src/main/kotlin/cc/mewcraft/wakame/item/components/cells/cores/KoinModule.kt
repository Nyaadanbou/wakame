package cc.mewcraft.wakame.item.components.cells.cores

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

internal fun moduleCores(): Module = module {
    // noop

    // empty
    single<EmptyCoreBootstrap> { EmptyCoreBootstrap } withOptions { bind<Initializable>() }

    // attributes
    single<AttributeCoreBootstrap> { AttributeCoreBootstrap } withOptions { bind<Initializable>() }
    single<AttributeCoreTooltipKeyProvider> { AttributeCoreTooltipKeyProvider(get()) }

    // skills
    single<SkillCoreBootstrap> { SkillCoreBootstrap } withOptions { bind<Initializable>() }
    single<SkillCoreTooltipKeyProvider> { SkillCoreTooltipKeyProvider(get()) }
}