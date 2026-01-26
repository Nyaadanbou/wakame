package cc.mewcraft.wakame.hook.impl.portals

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.teleport.RandomTeleport

@Hook(plugins = ["Portals"])
object PortalsHook {

    init {
        // 注册 Portals 提供的 RandomTeleport 实现
        RandomTeleport.setImplementation(PortalsRandomTeleport())
    }
}