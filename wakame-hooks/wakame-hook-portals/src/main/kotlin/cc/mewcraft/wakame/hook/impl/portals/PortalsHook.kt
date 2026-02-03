package cc.mewcraft.wakame.hook.impl.portals

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.teleport.RandomTeleport
import net.thenextlvl.portals.action.ActionType
import net.thenextlvl.portals.action.ActionTypeRegistry

@Hook(plugins = ["Portals"])
object PortalsHook {

    init {
        // 注册 Portals 提供的 RandomTeleport 实现
        RandomTeleport.setImplementation(PortalsRandomTeleport())
        // 注册 Portals 的新 ActionType
        ActionTypeRegistry.registry().register(ActionType.create("open_menu", String::class.java, OpenMenuAction()))
    }
}