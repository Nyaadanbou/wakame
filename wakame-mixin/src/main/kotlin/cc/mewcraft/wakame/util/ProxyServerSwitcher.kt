package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.PluginHolder
import org.bukkit.entity.Player
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

/**
 * 用于在代理服务器环境下切换服务器的接口.
 */
interface ProxyServerSwitcher {

    /**
     * 将玩家 [player] 传送到目标服务器 [targetServer].
     */
    fun switch(player: Player, targetServer: String)

    /**
     * 该伴生对象持有了 [ProxyServerSwitcher] 的当前实现.
     */
    companion object : ProxyServerSwitcher {
        private var implementation: ProxyServerSwitcher? = null

        fun setImplementation(impl: ProxyServerSwitcher) {
            this.implementation = impl
        }

        override fun switch(player: Player, targetServer: String) {
            implementation?.switch(player, targetServer)
        }
    }
}

/**
 * 基于 BungeeCord 插件消息通道实现的 [ProxyServerSwitcher].
 *
 * 协议格式见 [Plugin Message Types](https://docs.papermc.io/paper/dev/plugin-messaging/#plugin-message-types).
 */
class BungeeCordProxyServerSwitcher : ProxyServerSwitcher {
    override fun switch(player: Player, targetServer: String) {
        val pluginMessage = ByteArrayOutputStream()
        val out = DataOutputStream(pluginMessage)
        out.writeUTF("Connect")
        out.writeUTF(targetServer)
        player.sendPluginMessage(PluginHolder.instance, "BungeeCord", pluginMessage.toByteArray())
    }
}