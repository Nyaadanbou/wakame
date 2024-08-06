package cc.mewcraft.wakame.reforge.merging

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun mergingModule(): Module = module {
    single { MergingTables } bind Initializable::class
}