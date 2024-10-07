package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun logicModule(): Module = module {
    single { AdventureLevelHotfix }
    single { ItemSlotChangeRegistry } bind Initializable::class
}