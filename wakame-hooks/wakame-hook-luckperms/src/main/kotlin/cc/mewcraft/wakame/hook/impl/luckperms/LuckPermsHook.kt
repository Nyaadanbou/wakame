package cc.mewcraft.wakame.hook.impl.luckperms

import cc.mewcraft.messaging2.ServerInfoProvider
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.permission.PermissionIntegration
import cc.mewcraft.wakame.monetization.MonetizationConfig
import net.luckperms.api.LuckPermsProvider

@Hook(plugins = ["LuckPerms"])
object LuckPermsHook : PermissionIntegration by LuckPermsPermissionIntegration() {

    init {
        ServerInfoProvider.setImplementation(LuckPermsServerInfoProvider())

        val luckpermsIntegration = MonetizationConfig.luckpermsIntegration
        if (luckpermsIntegration.enabled) {
            LuckPermsProvider.get().contextManager.registerCalculator(
                MonetizationContextCalculator(luckpermsIntegration.paidAboveThresholds)
            )
        }
    }
}
