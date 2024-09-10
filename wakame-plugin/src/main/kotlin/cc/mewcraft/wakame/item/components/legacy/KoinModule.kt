package cc.mewcraft.wakame.item.components.legacy

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

internal fun moduleLegacy(): Module = module {
    single { ItemMetaBootstrap } withOptions { bind<Initializable>() }
}