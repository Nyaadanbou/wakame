package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun moddingModule(): Module = module {
    single { ModdingTableRegistry } bind Initializable::class
}