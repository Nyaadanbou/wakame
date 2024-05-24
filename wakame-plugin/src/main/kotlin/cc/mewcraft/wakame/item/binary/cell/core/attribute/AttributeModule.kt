package cc.mewcraft.wakame.item.binary.cell.core.attribute

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

internal fun attributeCoreModule(): Module = module {
    single<AttributeLineKeyFactory> { AttributeLineKeyFactory(get()) }
    single<AttributeCoreInitializer> { AttributeCoreInitializer } withOptions { bind<Initializable>() }
}