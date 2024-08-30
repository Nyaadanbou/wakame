package cc.mewcraft.wakame.player

import cc.mewcraft.wakame.player.component.ComponentListener
import cc.mewcraft.wakame.player.interact.FuckOffHandListener
import cc.mewcraft.wakame.player.inventory.ItemSlotWatcher
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun playerModule(): Module = module {
    single<FuckOffHandListener> { FuckOffHandListener() }
    single<ItemSlotWatcher> { ItemSlotWatcher() }
    single<ComponentListener> { ComponentListener() }
}