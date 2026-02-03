package cc.mewcraft.wakame.init

import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.messaging2.ReactiveMessagingConfiguration
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.messaging.MessagingManager

@get:JvmName("messagingConfig")
val MESSAGING_CONFIG = ConfigAccess["messaging"]

@Init(InitStage.POST_WORLD)
internal object MessagingInitializer {

    @InitFun
    fun init() {
        MessagingManager.init(ReactiveMessagingConfiguration(MESSAGING_CONFIG))
        MessagingManager.start()
    }

    @DisableFun
    fun disable() {
        MessagingManager.shutdown()
    }
}
