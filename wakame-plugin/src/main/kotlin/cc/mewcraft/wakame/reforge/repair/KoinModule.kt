package cc.mewcraft.wakame.reforge.repair

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun repairModule(): Module = module {
    single { RepairingTableRegistry } bind Initializable::class
}