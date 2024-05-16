package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun attributeModule(): Module = module {
    singleOf(::AttributeEventHandler)
    single<Initializable> { DefaultAttributes }
}