package cc.mewcraft.wakame.hook.impl.luckperms

import cc.mewcraft.messaging2.ServerInfoProvider
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.permission.PermissionIntegration

@Hook(plugins = ["LuckPerms"])
object LuckPermsHook : PermissionIntegration by LuckPermsPermissionIntegration() {

    init {
        ServerInfoProvider.setImplementation(LuckPermsServerInfoProvider())
    }
}
