package cc.mewcraft.wakame.reforge.recycle

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun recycleModule(): Module = module {
    single { RecyclingStationRegistry } bind Initializable::class
}