package cc.mewcraft.wakame.hook.impl.towny.messaging

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.integration.townynetwork.TownyNetworkIntegration
import cc.mewcraft.wakame.messaging.MessagingManager
import cc.mewcraft.wakame.messaging.packet.*
import cc.mewcraft.wakame.util.ProxyServerSwitcher
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Resident
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import java.time.Duration
import java.util.*

private val townyApi: TownyAPI
    get() = TownyAPI.getInstance()

private val serverId: UUID
    get() = MessagingManager.serverId

private val serverKey: String
    get() = MessagingManager.serverKey

private val serverGroup: String
    get() = MessagingManager.serverGroup

object TownyNetworkImplementations : TownyNetworkIntegration, TownyNetworkHandler, Listener {

    override suspend fun requestTeleportTown(player: Player, targetServer: String) {
        TownyTeleportImplementation.requestTeleportTown(player, targetServer)
    }

    override suspend fun requestTeleportNation(player: Player, targetServer: String) {
        TownyTeleportImplementation.requestTeleportNation(player, targetServer)
    }

    override fun handle(packet: TownSpawnRequestPacket) {
        TownyTeleportImplementation.handle(packet)
    }

    override fun handle(packet: TownSpawnResponsePacket) {
        TownyTeleportImplementation.handle(packet)
    }

    override fun handle(packet: NationSpawnRequestPacket) {
        TownyTeleportImplementation.handle(packet)
    }

    override fun handle(packet: NationSpawnResponsePacket) {
        TownyTeleportImplementation.handle(packet)
    }

    @EventHandler
    private fun on(event: PlayerSpawnLocationEvent) {
        TownyTeleportImplementation.on(event)
    }
}

// Towny 跨服传送部分的具体实现
// 单独写个类是为了让逻辑清晰一些
private object TownyTeleportImplementation {

    private val townTeleportSessionCache: Cache<UUID, TownTeleportSession> = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(5))
        .build()

    private val nationTeleportSessionCache: Cache<UUID, NationTeleportSession> = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(5))
        .build()

    fun requestTeleportTown(player: Player, targetServer: String) {
        val playerId = player.uniqueId

        // 如果有未完成的传送请求, 则 return
        if (this.townTeleportSessionCache.getIfPresent(playerId) != null) {
            player.sendMessage(TranslatableMessages.MSG_ERR_NETWORK_TELEPORT_REQUEST_ALREADY_PENDING)
            return
        }

        // 创建会话
        this.townTeleportSessionCache.put(playerId, TownTeleportSession(TownTeleportSession.Stage.AWAITING_RESPONSE, targetServer))

        // 广播封包
        MessagingManager.queuePacket { TownSpawnRequestPacket(serverId, playerId, targetServer) }
    }

    fun requestTeleportNation(player: Player, targetServer: String) {
        val playerId = player.uniqueId

        // 如果有未完成的传送请求, 则 return
        if (this.nationTeleportSessionCache.getIfPresent(playerId) != null) {
            player.sendMessage(TranslatableMessages.MSG_ERR_NETWORK_TELEPORT_REQUEST_ALREADY_PENDING)
            return
        }

        // 创建会话
        this.nationTeleportSessionCache.put(playerId, NationTeleportSession(NationTeleportSession.Stage.AWAITING_RESPONSE, targetServer))

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
        this.townTeleportSessionCache.put(playerId, TownTeleportSession(TownTeleportSession.Stage.READY_TO_TELEPORT, targetServer))

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
        val session = this.townTeleportSessionCache.asMap().remove(playerId) ?: return
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

        this.nationTeleportSessionCache.put(playerId, NationTeleportSession(NationTeleportSession.Stage.READY_TO_TELEPORT, targetServer))
        MessagingManager.queuePacket { TownSpawnResponsePacket(serverId, playerId, TownSpawnResponsePacket.ResponseType.ALLOW) }
    }

    fun handle(packet: NationSpawnResponsePacket) {
        val playerId = packet.playerId
        val session = this.nationTeleportSessionCache.asMap().remove(playerId) ?: return
        val player = Bukkit.getPlayer(playerId) ?: return
        when (packet.response) {
            NationSpawnResponsePacket.ResponseType.ALLOW -> ProxyServerSwitcher.switch(player, session.targetServer)
            NationSpawnResponsePacket.ResponseType.DENY_FOR_NO_NATION -> player.sendMessage(TranslatableMessages.MSG_ERR_NO_NATION_AT_TARGET_SERVER)
        }
    }

    fun on(event: PlayerSpawnLocationEvent) {
        // TODO #441 切换成 AsyncPlayerSpawnLocationEvent 并确保线程安全

        // 当玩家进入服务器时:
        // 根据正在排队的传送请求, 将玩家传送到他的城镇/国家传送点

        fun handleTownTeleport(playerId: UUID, resident: Resident, setSpawn: (Location) -> Unit) {
            val session = this.townTeleportSessionCache.asMap().remove(playerId) ?: return
            if (session.stage != TownTeleportSession.Stage.READY_TO_TELEPORT) return
            val town = resident.townOrNull ?: return
            val townSpawn = town.spawnOrNull ?: return
            setSpawn(townSpawn)
            session.stage = TownTeleportSession.Stage.TELEPORTED
        }

        fun handleNationTeleport(playerId: UUID, resident: Resident, setSpawn: (Location) -> Unit) {
            val session = this.nationTeleportSessionCache.asMap().remove(playerId) ?: return
            if (session.stage != NationTeleportSession.Stage.READY_TO_TELEPORT) return
            val nation = resident.nationOrNull ?: return
            val nationSpawn = nation.spawnOrNull ?: return
            setSpawn(nationSpawn)
            session.stage = NationTeleportSession.Stage.TELEPORTED
        }

        val playerId = event.player.uniqueId
        val resident = townyApi.getResident(playerId) ?: return
        handleTownTeleport(playerId, resident, event::setSpawnLocation)
        handleNationTeleport(playerId, resident, event::setSpawnLocation)
    }

    // 封装了一次跨服城镇传送请求所需要的所有信息
    // 每个服务器上都维护了自己的一份 session
    private class TownTeleportSession(
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
    private class NationTeleportSession(
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