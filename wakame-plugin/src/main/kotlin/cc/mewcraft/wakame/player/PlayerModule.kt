package cc.mewcraft.wakame.player

import cc.mewcraft.wakame.player.attackspeed.AttackSpeedEventHandler
import cc.mewcraft.wakame.player.component.ComponentListener
import cc.mewcraft.wakame.player.equipment.ArmorChangeEventSupport
import cc.mewcraft.wakame.player.interact.FuckOffHandListener
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun playerModule(): Module = module {
    singleOf(::FuckOffHandListener)
    singleOf(::ComponentListener)
    singleOf(::AttackSpeedEventHandler)
    single { ArmorChangeEventSupport }
}