@file:JvmName("TownyNetwork")

package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.messaging2.ServerInfoProvider
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.feature.ProxyServerSwitcher
import cc.mewcraft.wakame.integration.townynetwork.TownyNetworkIntegration
import cc.mewcraft.wakame.messaging.MessagingManager
import cc.mewcraft.wakame.messaging.handler.TownyNetworkPacketHandler
import cc.mewcraft.wakame.messaging.packet.NationSpawnRequestPacket
import cc.mewcraft.wakame.messaging.packet.NationSpawnResponsePacket
import cc.mewcraft.wakame.messaging.packet.TownSpawnRequestPacket
import cc.mewcraft.wakame.messaging.packet.TownSpawnResponsePacket
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalCause
import com.google.common.cache.RemovalNotification
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Resident
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import java.time.Duration
import java.util.*

private val townyApi: TownyAPI
    get() = TownyAPI.getInstance()
private val serverId: UUID
    get() = ServerInfoProvider.serverId
private val serverKey: String
    get() = ServerInfoProvider.serverKey
private val serverGroup: String
    get() = ServerInfoProvider.serverGroup

/**
 * 服务器上有安装 Towny 时的实现.
 */
object TownyNetworkImpl : TownyNetworkIntegration, TownyNetworkPacketHandler, Listener {

    override suspend fun reqTownSpawn(player: Player, targetServer: String) =
        TownyTeleportImpl.reqTownSpawn(player, targetServer)

    override suspend fun reqNationSpawn(player: Player, targetServer: String) =
        TownyTeleportImpl.reqNationSpawn(player, targetServer)

    override fun handle(packet: TownSpawnRequestPacket) =
        TownyTeleportImpl.handle(packet)

    override fun handle(packet: TownSpawnResponsePacket) =
        TownyTeleportImpl.handle(packet)

    override fun handle(packet: NationSpawnRequestPacket) =
        TownyTeleportImpl.handle(packet)

    override fun handle(packet: NationSpawnResponsePacket) =
        TownyTeleportImpl.handle(packet)

    @EventHandler
    private fun on(event: AsyncPlayerSpawnLocationEvent) =
        TownyTeleportImpl.on(event)
}

// 跨服传送部分的实现
private object TownyTeleportImpl {

    private val townSessions: Cache<UUID, TownSession> = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(5))
        .removalListener<UUID, TownSession>(::notifyRequestExpiration)
        .build()
    private val nationSessions: Cache<UUID, NationSession> = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(5))
        .removalListener<UUID, NationSession>(::notifyRequestExpiration)
        .build()

    fun reqTownSpawn(player: Player, targetServer: String) {
        // 如果目标服务器是本服务器, 则直接本地传送
        if (targetServer == serverKey) {
            handleLocalTownSpawn(player)
            return
        }

        val playerId = player.uniqueId

        // 如果有未完成的传送请求, 则 return
        if (this.townSessions.getIfPresent(playerId) != null) {
            player.sendMessage(TranslatableMessages.MSG_ERR_NETWORK_TELEPORT_REQUEST_ALREADY_PENDING)
            return
        }

        // 创建会话
        this.townSessions.put(playerId, TownSession(TownSession.Stage.AWAITING_RESPONSE, targetServer))

        // 广播封包
        MessagingManager.queuePacket { TownSpawnRequestPacket(serverId, playerId, targetServer) }
    }

    fun reqNationSpawn(player: Player, targetServer: String) {
        // 如果目标服务器是本服务器, 则直接本地传送
        if (targetServer == serverKey) {
            handleLocalNationSpawn(player)
            return
        }

        val playerId = player.uniqueId

        // 如果有未完成的传送请求, 则 return
        if (this.nationSessions.getIfPresent(playerId) != null) {
            player.sendMessage(TranslatableMessages.MSG_ERR_NETWORK_TELEPORT_REQUEST_ALREADY_PENDING)
            return
        }

        // 创建会话
        this.nationSessions.put(playerId, NationSession(NationSession.Stage.AWAITING_RESPONSE, targetServer))

        // 广播封包
        MessagingManager.queuePacket { NationSpawnRequestPacket(serverId, playerId, targetServer) }
    }

    fun handle(packet: TownSpawnRequestPacket) {
        val targetServer = packet.targetServer

        // 如果请求不由本服务器负责, 则 return
        if (targetServer != serverKey) return
        val playerId = packet.playerId

        // resident 为 null 时通常说明玩家从没来过本服务器
        val resident = townyApi.getResident(playerId)
        // town 为 null 时说明玩家不属于任何城镇
        val town = resident?.townOrNull

        // 如果玩家不属于任何城镇, 则广播“拒绝传送”, 通知请求方无法进行传送
        if (resident == null || town == null) {
            MessagingManager.queuePacket { TownSpawnResponsePacket(serverId, playerId, TownSpawnResponsePacket.ResponseType.DENY_FOR_NO_TOWN) }
            return
        }

        // 创建对话, 记录为“该玩家可以进行传送”
        this.townSessions.put(playerId, TownSession(TownSession.Stage.READY_TO_TELEPORT, targetServer))

        // 广播“允许传送”, 让请求方接着处理 (也就是让请求方把玩家送到当前服务器)
        // 注意: 这里之所以让请求方把玩家送过来, 而不是让当前服务器把玩家送过来,
        //  是因为正常情况下玩家肯定位于请求方服务器, 这时可以利用 Plugin Message (Connect)
        //  将玩家转移到新的服务器. 不让当前服务器转移玩家是因为当前服务器可能没人,
        //  因此 Plugin Message 也就根本无法使用.
        //  同样的原因适用于跨服国家传送.
        MessagingManager.queuePacket { TownSpawnResponsePacket(serverId, playerId, TownSpawnResponsePacket.ResponseType.ALLOW) }
    }

    fun handle(packet: TownSpawnResponsePacket) {
        val playerId = packet.playerId
        val session = this.townSessions.asMap().remove(playerId) ?: return
        val player = Bukkit.getPlayer(playerId) ?: return
        when (packet.response) {
            TownSpawnResponsePacket.ResponseType.ALLOW -> ProxyServerSwitcher.switch(player, session.targetServer)
            TownSpawnResponsePacket.ResponseType.DENY_FOR_NO_TOWN -> player.sendMessage(TranslatableMessages.MSG_ERR_NO_TOWN_AT_TARGET_SERVER)
        }
    }

    fun handle(packet: NationSpawnRequestPacket) {
        val targetServer = packet.targetServer
        if (targetServer != serverKey) return
        val playerId = packet.playerId
        val resident = townyApi.getResident(playerId)
        val nation = resident?.nationOrNull
        if (resident == null || nation == null) {
            MessagingManager.queuePacket { NationSpawnResponsePacket(serverId, playerId, NationSpawnResponsePacket.ResponseType.DENY_FOR_NO_NATION) }
            return
        }

        this.nationSessions.put(playerId, NationSession(NationSession.Stage.READY_TO_TELEPORT, targetServer))
        MessagingManager.queuePacket { TownSpawnResponsePacket(serverId, playerId, TownSpawnResponsePacket.ResponseType.ALLOW) }
    }

    fun handle(packet: NationSpawnResponsePacket) {
        val playerId = packet.playerId
        val session = this.nationSessions.asMap().remove(playerId) ?: return
        val player = Bukkit.getPlayer(playerId) ?: return
        when (packet.response) {
            NationSpawnResponsePacket.ResponseType.ALLOW -> ProxyServerSwitcher.switch(player, session.targetServer)
            NationSpawnResponsePacket.ResponseType.DENY_FOR_NO_NATION -> player.sendMessage(TranslatableMessages.MSG_ERR_NO_NATION_AT_TARGET_SERVER)
        }
    }

    fun on(event: AsyncPlayerSpawnLocationEvent) {
        // 当玩家进入服务器时:
        // 根据正在排队的传送请求, 将玩家传送到他的城镇/国家传送点

        val playerId = event.connection.profile.id ?: return
        val resident = townyApi.getResident(playerId) ?: return
        handleNetworkTownSpawn(playerId, resident, event::setSpawnLocation)
        handleNetworkNationSpawn(playerId, resident, event::setSpawnLocation)
    }

    private fun handleNetworkTownSpawn(playerId: UUID, resident: Resident, locationConsumer: (Location) -> Unit) {
        val session = this.townSessions.asMap().remove(playerId) ?: return
        if (session.stage != TownSession.Stage.READY_TO_TELEPORT) return
        val town = resident.townOrNull ?: return
        val townSpawn = town.spawnOrNull ?: return
        locationConsumer(townSpawn)
        session.stage = TownSession.Stage.TELEPORTED
    }

    private fun handleNetworkNationSpawn(playerId: UUID, resident: Resident, locationConsumer: (Location) -> Unit) {
        val session = this.nationSessions.asMap().remove(playerId) ?: return
        if (session.stage != NationSession.Stage.READY_TO_TELEPORT) return
        val nation = resident.nationOrNull ?: return
        val nationSpawn = nation.spawnOrNull ?: return
        locationConsumer(nationSpawn)
        session.stage = NationSession.Stage.TELEPORTED
    }

    private fun handleLocalTownSpawn(player: Player) {
        val spawn = townyApi.getTown(player)?.spawnOrNull
        if (spawn == null) {
            player.sendMessage(TranslatableMessages.MSG_ERR_NO_TOWN_AT_TARGET_SERVER)
            return
        }
        player.teleportAsync(spawn, PlayerTeleportEvent.TeleportCause.PLUGIN)
    }

    private fun handleLocalNationSpawn(player: Player) {
        val spawn = townyApi.getNation(player)?.spawnOrNull
        if (spawn == null) {
            player.sendMessage(TranslatableMessages.MSG_ERR_NO_NATION_AT_TARGET_SERVER)
            return
        }
        player.teleportAsync(spawn, PlayerTeleportEvent.TeleportCause.PLUGIN)
    }

    private fun <V : Any> notifyRequestExpiration(notification: RemovalNotification<UUID, V>) {
        val k = notification.key
        val v = notification.value
        val c = notification.cause
        if (k != null && v != null && c == RemovalCause.EXPIRED) {
            Bukkit.getPlayer(k)?.sendMessage(TranslatableMessages.MSG_ERR_NETWORK_TELEPORT_REQUEST_EXPIRED)
        }
    }

    // 封装了一次跨服城镇传送请求所需要的所有信息
    // 每个服务器上都维护了自己的一份 session
    private class TownSession(
        var stage: Stage,
        val targetServer: String,
    ) {

        enum class Stage {
            AWAITING_RESPONSE,
            READY_TO_TELEPORT,
            TELEPORTED,
        }
    }

    // 封装了一次跨服国家传送请求所需要的所有信息
    private class NationSession(
        var stage: Stage,
        val targetServer: String,
    ) {

        enum class Stage {
            AWAITING_RESPONSE,
            READY_TO_TELEPORT,
            TELEPORTED,
        }
    }
}