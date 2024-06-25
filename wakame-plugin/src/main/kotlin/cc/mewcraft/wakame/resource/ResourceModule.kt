package cc.mewcraft.wakame.resource

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun resourceModule() = module {
    singleOf(::ResourceListener)
}