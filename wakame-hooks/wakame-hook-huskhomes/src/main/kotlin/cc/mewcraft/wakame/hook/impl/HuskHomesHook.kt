package cc.mewcraft.wakame.hook.impl

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.teleport.NetworkTeleport
import cc.mewcraft.wakame.util.registerEvents

@Hook(plugins = ["HuskHomes"])
object HuskHomesHook {

    init {
        // 注册事件监听器
        TpaBlockListener().registerEvents()
        // 注册跨服务器传送实现
        NetworkTeleport.setImplementation(HuskHomesNetworkTeleport())
    }
}
