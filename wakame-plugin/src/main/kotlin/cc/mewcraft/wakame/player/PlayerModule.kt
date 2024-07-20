package cc.mewcraft.wakame.player

import cc.mewcraft.wakame.player.interact.FuckOffHandListener
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun playerModule(): Module = module {
    single<FuckOffHandListener> {
        FuckOffHandListener()
    }
}