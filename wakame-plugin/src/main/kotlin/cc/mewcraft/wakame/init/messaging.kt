package cc.mewcraft.wakame.init

import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.messaging.MessagingManager

@Init(InitStage.POST_WORLD)
internal object MessagingInitializer {

    @InitFun
    fun init() {
        MessagingManager.init()
    }

    @DisableFun
    fun disable() {
        MessagingManager.shutdown()
    }
}
