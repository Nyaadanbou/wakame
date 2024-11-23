package cc.mewcraft.wakame.compatibility.chestshort

import org.koin.core.module.Module
import org.koin.dsl.module

fun chestSortModule(): Module = module {
    single { ChestSortListener }
}