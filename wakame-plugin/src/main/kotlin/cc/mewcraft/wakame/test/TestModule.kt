package cc.mewcraft.wakame.test

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun testModule(): Module = module {
    singleOf(::TestListener)
}