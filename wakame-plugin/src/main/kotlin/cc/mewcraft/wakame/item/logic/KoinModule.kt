package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.user.PlayerResourceFix
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun logicModule(): Module = module {
    single { PlayerResourceFix }
}