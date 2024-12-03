package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.user.PlayerLevelListener
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun logicModule(): Module = module {
    single { PlayerLevelListener }
    single { ItemSlotChangeRegistry } bind Initializable::class
}