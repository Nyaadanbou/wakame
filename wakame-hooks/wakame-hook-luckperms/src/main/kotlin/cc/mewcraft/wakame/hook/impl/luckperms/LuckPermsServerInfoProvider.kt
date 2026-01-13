package cc.mewcraft.wakame.hook.impl.luckperms

import cc.mewcraft.messaging2.ServerInfoProvider
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import java.util.*
import kotlin.jvm.optionals.getOrDefault

/**
 * 该实现假设 `LuckPerms/contexts.json` 中存在以下类似的定义:
 *
 * ```json
 * {
 *   "static-contexts": {
 *     "server-id": "xhome1",
 *     "server-group": "xhome",
 *     "server-name": "家园"
 *   },
 *   "default-contexts": {}
 * }
 * ```
 */
internal class LuckPermsServerInfoProvider : ServerInfoProvider {

    private val luckPermsApi: LuckPerms
        get() = LuckPermsProvider.get()

    override val serverId: UUID = UUID.randomUUID()

    override val serverKey: String
        get() = luckPermsApi.contextManager.staticContext.getAnyValue("server-id").getOrDefault("unset")
    override val serverGroup: String
        get() = luckPermsApi.contextManager.staticContext.getAnyValue("server-group").getOrDefault("unset")
    override val serverName: String
        get() = luckPermsApi.contextManager.staticContext.getAnyValue("server-name").getOrDefault("unset")
}