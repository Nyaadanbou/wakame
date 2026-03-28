package cc.mewcraft.wakame.hook.impl.malts

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.virtualstorage.VirtualStorageIntegration

@Hook(plugins = ["Malts"])
object MaltsHook {

    init {
        VirtualStorageIntegration.setImplementation(VirtualStorageIntegrationImpl())
    }
}