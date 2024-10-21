package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun mergingModule(): Module = module {
    single { MergingTableRegistry } bind Initializable::class
}