package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun resourceModule() = module {
    singleOf(::ResourceTicker) bind Initializable::class
}