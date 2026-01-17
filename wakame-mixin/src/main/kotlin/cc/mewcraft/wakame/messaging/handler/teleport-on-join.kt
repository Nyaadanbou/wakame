package cc.mewcraft.wakame.messaging.handler

import cc.mewcraft.messaging2.ServerInfoProvider
import cc.mewcraft.messaging2.packet.SimplePacketHandler
import cc.mewcraft.wakame.messaging.packet.TeleportOnJoinRequestPacket
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.*
import java.util.concurrent.TimeUnit


object TeleportOnJoinPacketHandler : SimplePacketHandler {

    private val pendingRequests: Cache<UUID, Unit> = CacheBuilder.newBuilder()
        .expireAfterWrite(30, TimeUnit.SECONDS)
        .build<UUID, Unit>()

    fun has(uuid: UUID): Boolean {
        return pendingRequests.getIfPresent(uuid) != null
    }

    fun clean(uuid: UUID) {
        pendingRequests.invalidate(uuid)
    }

    fun handle(packet: TeleportOnJoinRequestPacket) {
        if (packet.group == ServerInfoProvider.serverGroup) {
            pendingRequests.put(packet.playerId, Unit)
        }
    }
}