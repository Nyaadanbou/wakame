package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.wakame.api.protection.ProtectionIntegration
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.towny.GovernmentListProvider
import cc.mewcraft.wakame.integration.townynetwork.TownyNetworkIntegration
import cc.mewcraft.wakame.messaging.handler.TownyNetworkPacketHandler
import cc.mewcraft.wakame.util.registerEvents

@Hook(plugins = ["Towny"])
object TownyHook : ProtectionIntegration by TownyProtectionIntegration {

    // Java interop fix - start
    override fun getExecutionMode(): ProtectionIntegration.ExecutionMode {
        return super.getExecutionMode()
    }
    // Java interop fix - end

    init {
        GovernmentListProvider.setImplementation(TownyGovListProvider())

        with(TownyNetworkImpl) {
            registerEvents()
            TownyNetworkIntegration.setImplementation(this)
            TownyNetworkPacketHandler.setImplementation(this)
        }
    }
}