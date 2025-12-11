package cc.mewcraft.wakame.hook.impl.luckperms

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.permission.PermissionIntegration
import cc.mewcraft.wakame.messaging.ServerInfoProvider

@Hook(plugins = ["LuckPerms"])
object LuckPermsHook : PermissionIntegration by LuckPermsPermissionIntegration() {

    init {
        ServerInfoProvider.setImplementation(LuckPermsServerInfoProvider())
    }
}
