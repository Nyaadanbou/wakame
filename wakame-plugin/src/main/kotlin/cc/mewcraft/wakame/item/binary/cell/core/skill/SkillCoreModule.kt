package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

internal fun skillCoreModule(): Module = module {
    single<SkillCoreInitializer> { SkillCoreInitializer } withOptions { bind<Initializable>() }
    single<SkillLineKeyFactory> { SkillLineKeyFactory(get()) }
}