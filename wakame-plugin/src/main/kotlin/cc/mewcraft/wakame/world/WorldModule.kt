// 本 package 负责所有游戏世界之内的逻辑实现
// TODO 逐步把现有的游戏世界内的逻辑迁移到这里

package cc.mewcraft.wakame.world

import cc.mewcraft.wakame.world.entity.BetterArmorStandListener
import cc.mewcraft.wakame.world.player.death.PlayerDeathProtect
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun worldModule(): Module = module {
    single { BetterArmorStandListener() }
    single { PlayerDeathProtect() }
}