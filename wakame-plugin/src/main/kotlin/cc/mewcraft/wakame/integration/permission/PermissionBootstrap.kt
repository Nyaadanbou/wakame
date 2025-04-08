package cc.mewcraft.wakame.integration.permission

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.integration.HooksLoader
import cc.mewcraft.wakame.lifecycle.LifecycleDispatcher
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(
    stage = InitStage.POST_WORLD,
    dispatcher = LifecycleDispatcher.ASYNC,
    runAfter = [HooksLoader::class]
)
internal object PermissionBootstrap {

    @InitFun
    fun init() {
        if (PermissionManager.integrations.size > 1) {
            LOGGER.warn("Multiple permission integrations have been registered: ${PermissionManager.integrations.joinToString { it::class.simpleName!! }}, Koish will use the first one")
        }
    }

}