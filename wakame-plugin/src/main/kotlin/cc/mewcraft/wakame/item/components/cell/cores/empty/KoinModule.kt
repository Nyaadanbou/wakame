package cc.mewcraft.wakame.item.components.cell.cores.empty

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

internal fun coreEmptyModule(): Module = module {
    single<EmptyCoreBootstrap> { EmptyCoreBootstrap } withOptions { bind<Initializable>() }
}