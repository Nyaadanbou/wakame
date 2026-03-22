package cc.mewcraft.wakame.integration.permission

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.integration.PostWorldHooksLoader
import cc.mewcraft.wakame.integration.PreWorldHooksLoader
import cc.mewcraft.wakame.lifecycle.LifecycleDispatcher
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

@Init(
    stage = InitStage.POST_WORLD,
    dispatcher = LifecycleDispatcher.ASYNC,
    runAfter = [PreWorldHooksLoader::class, PostWorldHooksLoader::class]
)
internal object PermissionBootstrap {

    @InitFun
    fun init() {
        if (PermissionManager.integrations.size > 1) {
            LOGGER.warn("Multiple permission integrations have been registered: ${PermissionManager.integrations.joinToString { it::class.simpleName!! }}, Koish will use the first one")
        }
    }

}