package cc.mewcraft.wakame.item.components.cells.cores.attribute

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

internal fun coreAttributeModule(): Module = module {
    single<AttributeCoreBootstrap> { AttributeCoreBootstrap } withOptions { bind<Initializable>() }
    single<AttributeLineKeyFactory> { AttributeLineKeyFactory(get()) }
}