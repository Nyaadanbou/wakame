package cc.mewcraft.wakame.item.binary.cell.core.empty

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

internal fun emptyCoreModule(): Module = module {
    single<EmptyCoreInitializer> { EmptyCoreInitializer } withOptions { bind<Initializable>() }
}