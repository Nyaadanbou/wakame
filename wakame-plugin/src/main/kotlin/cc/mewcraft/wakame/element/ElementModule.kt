package cc.mewcraft.wakame.element

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun elementModule(): Module = module {
    singleOf(::ElementSerializer)
}