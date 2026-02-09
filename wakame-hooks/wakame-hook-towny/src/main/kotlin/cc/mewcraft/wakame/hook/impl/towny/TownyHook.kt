package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.wakame.api.protection.ProtectionIntegration
import cc.mewcraft.wakame.api.protection.ProtectionIntegration.ExecutionMode
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.towny.TownyLocal
import cc.mewcraft.wakame.integration.townynetwork.TownyNetworkIntegration
import cc.mewcraft.wakame.messaging.handler.TownyNetworkPacketHandler
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import cc.mewcraft.wakame.util.registerEvents

@Hook(plugins = ["Towny"])
object TownyHook : ProtectionIntegration by TownyProtectionIntegration {

    // Java interop fix - start
    override fun getExecutionMode(): ExecutionMode {
        return super.getExecutionMode()
    }
    // Java interop fix - end

    init {
        // 只要反序列化发生的时机晚于该函数调用就可以正常反序列化
        ConfigAccess.registerSerializer(KOISH_NAMESPACE, EntryFilter.serializer())

        TownyLocal.setImplementation(TownyTownyLocal())

        with(TownyNetworkImpl) {
            registerEvents()
            TownyNetworkIntegration.setImplementation(this)
            TownyNetworkPacketHandler.setImplementation(this)
        }
    }
}