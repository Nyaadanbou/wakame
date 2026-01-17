package cc.mewcraft.wakame.feature

import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.entry
import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import cc.mewcraft.messaging2.ServerInfoProvider
import cc.mewcraft.wakame.messaging.MessagingManager
import cc.mewcraft.wakame.messaging.handler.TeleportOnJoinPacketHandler
import cc.mewcraft.wakame.messaging.packet.TeleportOnJoinRequestPacket
import cc.mewcraft.wakame.util.ProxyServerSwitcher
import cc.mewcraft.wakame.util.runTaskLater
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.*


// 玩家进入服务器时, 修改其出现在世界里的位置坐标


object TeleportOnJoin {

    /**
     * 请求跨服传送.
     *
     * @param playerId 玩家 UUID
     * @param key 服务器的名字, 值来自 [ServerInfoProvider.serverKey]
     * @param group 服务器的所属组, 值来自 [ServerInfoProvider.serverGroup]
     */
    fun request(playerId: UUID, key: String, group: String) {
        MessagingManager.queuePacket {
            TeleportOnJoinRequestPacket(ServerInfoProvider.serverId, playerId, group)
        }
        runTaskLater(30) task@{
            val player = Bukkit.getPlayer(playerId) ?: return@task
            ProxyServerSwitcher.switch(player, key)
        }
    }
}


class TeleportOnJoinListener : Listener {

    /**
     * 配置文件.
     */
    val config: TeleportOnJoinConfig by MAIN_CONFIG.entry("teleport_on_join_explicit_server")

    // TODO 等升级到 Paper 1.21.11 时更换为 AsyncPlayerSpawnLocationEvent
    @EventHandler
    fun on(event: PlayerSpawnLocationEvent) {
        val player = event.player
        val playerId = player.uniqueId
        if (TeleportOnJoinPacketHandler.has(playerId) && config.conditions.all { condition -> condition.test(player) }) {
            TeleportOnJoinPacketHandler.clean(playerId)
            event.spawnLocation = config.target
        }
    }
}


/**
 * 代表玩家进入服务器时进行传送的条件.
 */
interface TeleportOnJoinCondition {

    fun test(player: Player): Boolean

    companion object {
        fun serializer(): SimpleSerializer<TeleportOnJoinCondition> {
            return DispatchingSerializer.createPartial(
                mapOf(
                    "server_group" to ServerGroup::class
                )
            )
        }
    }

    @ConfigSerializable
    data class ServerGroup(
        val group: String,
    ) : TeleportOnJoinCondition {

        override fun test(player: Player): Boolean {
            return group == ServerInfoProvider.serverGroup
        }
    }
}


/**
 * 玩家进入服务器时传送的配置结构.
 */
@ConfigSerializable
data class TeleportOnJoinConfig(
    /**
     * 是否启用该功能.
     */
    val enabled: Boolean = false,
    /**
     * 执行传送的条件.
     */
    val conditions: List<TeleportOnJoinCondition> = emptyList(),
    /**
     * 传送到的位置列表.
     */
    val locations: List<Location> = emptyList(),
) {
    /**
     * 随机选择的目标位置. (每次反序列化就固定)
     */
    val target: Location = locations.random()
}
