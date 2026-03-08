package cc.mewcraft.wakame.init

import cc.mewcraft.wakame.entity.player.KoishUserListener
import cc.mewcraft.wakame.entity.player.ServerOnlineUserTicker
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents

// TODO 迁移逻辑

@Init(InitStage.POST_WORLD)
object EntityInitializer {

    @InitFun
    fun init() {
        KoishUserListener.registerEvents()
        ServerOnlineUserTicker.registerEvents()
    }
}