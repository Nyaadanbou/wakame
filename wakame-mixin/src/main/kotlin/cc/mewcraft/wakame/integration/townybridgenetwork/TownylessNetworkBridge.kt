package cc.mewcraft.wakame.integration.townybridgenetwork

import cc.mewcraft.messaging2.ServerInfoProvider
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.feature.ProxyServerSwitcher
import cc.mewcraft.wakame.messaging.MessagingManager
import cc.mewcraft.wakame.messaging.handler.TownyBridgeNetworkPacketHandler
import cc.mewcraft.wakame.messaging.packet.*
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.Duration
import java.util.*

/**
 * 服务器上没有安装 Towny 时的实现.
 */
internal object TownylessNetworkBridge : TownyNetworkBridge, TownyBridgeNetworkPacketHandler {

    override suspend fun reqTownSpawn(player: Player, targetServer: String) =
        TownylessTeleportImpl.requestTeleportTown(player, targetServer)

    override suspend fun reqTownOutpost(player: Player, index: Int, targetServer: String) =
        TownylessTeleportImpl.requestTeleportTownOutpost(player, index, targetServer)

    override suspend fun reqNationSpawn(player: Player, targetServer: String) =
        TownylessTeleportImpl.requestTeleportNation(player, targetServer)

    override fun handle(packet: TownSpawnRequestPacket) =
        TownylessTeleportImpl.handle(packet)

    override fun handle(packet: TownSpawnResponsePacket) =
        TownylessTeleportImpl.handle(packet)

    override fun handle(packet: TownOutpostRequestPacket) =
        TownylessTeleportImpl.handle(packet)

    override fun handle(packet: TownOutpostResponsePacket) =
        TownylessTeleportImpl.handle(packet)

    override fun handle(packet: NationSpawnRequestPacket) =
        TownylessTeleportImpl.handle(packet)

    override fun handle(packet: NationSpawnResponsePacket) =
        TownylessTeleportImpl.handle(packet)
}

// 跨服传送部分的实现
private object TownylessTeleportImpl {
    private val serverId: UUID
        get() = ServerInfoProvider.serverId

    // expireAfterAccess 设置为 5 秒可以让玩家在请求传送后的一段时间内不能重复请求传送
    private val sessions: Cache<UUID, Session> = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(5))
        .build()

    fun requestTeleportTown(player: Player, targetServer: String) {
        val playerId = player.uniqueId
        if (this.sessions.getIfPresent(playerId) != null) {
            player.sendMessage(TranslatableMessages.MSG_ERR_NETWORK_TELEPORT_REQUEST_ALREADY_PENDING)
            return
        }
        this.sessions.put(playerId, Session(targetServer))
        MessagingManager.queuePacket { TownSpawnRequestPacket(serverId, playerId, targetServer) }
    }

    fun requestTeleportNation(player: Player, targetServer: String) {
        val playerId = player.uniqueId
        if (this.sessions.getIfPresent(playerId) != null) {
            player.sendMessage(TranslatableMessages.MSG_ERR_NETWORK_TELEPORT_REQUEST_ALREADY_PENDING)
            return
        }
        this.sessions.put(playerId, Session(targetServer))
        MessagingManager.queuePacket { NationSpawnRequestPacket(serverId, playerId, targetServer) }
    }

    fun requestTeleportTownOutpost(player: Player, index: Int, targetServer: String) {
        val playerId = player.uniqueId
        if (this.sessions.getIfPresent(playerId) != null) {
            player.sendMessage(TranslatableMessages.MSG_ERR_NETWORK_TELEPORT_REQUEST_ALREADY_PENDING)
            return
        }
        this.sessions.put(playerId, Session(targetServer))
        MessagingManager.queuePacket { TownOutpostRequestPacket(serverId, playerId, targetServer, index) }
    }

    fun handle(
        @Suppress("UNUSED_PARAMETER")
        packet: TownSpawnRequestPacket
    ) {
        // 该实现不处理请求封包
    }

    fun handle(packet: TownSpawnResponsePacket) {
        val playerId = packet.playerId
        val session = this.sessions.asMap().remove(playerId) ?: return
        val player = Bukkit.getPlayer(playerId) ?: return
        when (packet.response) {
            TownSpawnResponsePacket.ResponseType.ALLOW -> ProxyServerSwitcher.switch(player, session.targetServer)
            TownSpawnResponsePacket.ResponseType.DENY_FOR_NO_TOWN -> player.sendMessage(TranslatableMessages.MSG_ERR_NO_TOWN_AT_TARGET_SERVER)
        }
    }

    fun handle(@Suppress("UNUSED_PARAMETER") packet: TownOutpostRequestPacket) {
        // 该实现不处理请求封包
    }

    fun handle(packet: TownOutpostResponsePacket) {
        val playerId = packet.playerId
        val session = this.sessions.asMap().remove(playerId) ?: return
        val player = Bukkit.getPlayer(playerId) ?: return
        when (packet.response) {
            TownOutpostResponsePacket.ResponseType.ALLOW -> ProxyServerSwitcher.switch(player, session.targetServer)
            TownOutpostResponsePacket.ResponseType.DENY_FOR_NO_TOWN -> player.sendMessage(TranslatableMessages.MSG_ERR_NO_TOWN_AT_TARGET_SERVER)
            TownOutpostResponsePacket.ResponseType.DENY_FOR_NO_SUCH_OUTPOST -> player.sendMessage(TranslatableMessages.MSG_ERR_TOWN_NOT_ENOUGH_OUTPOSTS)
        }
    }

    fun handle(
        @Suppress("UNUSED_PARAMETER")
        packet: NationSpawnRequestPacket
    ) {
        // 该实现不处理请求封包
    }

    fun handle(packet: NationSpawnResponsePacket) {
        val playerId = packet.playerId
        val session = this.sessions.asMap().remove(playerId) ?: return
        val player = Bukkit.getPlayer(playerId) ?: return
        when (packet.response) {
            NationSpawnResponsePacket.ResponseType.ALLOW -> ProxyServerSwitcher.switch(player, session.targetServer)
            NationSpawnResponsePacket.ResponseType.DENY_FOR_NO_NATION -> player.sendMessage(TranslatableMessages.MSG_ERR_NO_NATION_AT_TARGET_SERVER)
        }
    }

    private data class Session(val targetServer: String)
}

// 其他跨服功能的实现...
