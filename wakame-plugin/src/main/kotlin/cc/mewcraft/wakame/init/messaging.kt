package cc.mewcraft.wakame.init

import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.messaging2.MessagingConfig
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.messaging.KoishMessagingManager

@Init(InitStage.POST_WORLD)
internal object MessagingInitializer {

    @InitFun
    fun init() {
        KoishMessagingManager.init(MessagingConfig(MAIN_CONFIG))
        KoishMessagingManager.start()
    }

    @DisableFun
    fun disable() {
        KoishMessagingManager.shutdown()
    }
}
