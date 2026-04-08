package cc.mewcraft.wakame.messaging.handler

import cc.mewcraft.messaging2.packet.SimplePacketHandler
import cc.mewcraft.wakame.messaging.packet.JEICompatSyncPacket
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.*
import java.util.concurrent.TimeUnit

object JEICompatPacketHandler : SimplePacketHandler {
    private val syncedPlayers: Cache<UUID, Unit> = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.SECONDS)
        .build()

    fun has(uuid: UUID): Boolean {
        return syncedPlayers.getIfPresent(uuid) != null
    }

    fun clean(uuid: UUID) {
        syncedPlayers.invalidate(uuid)
    }

    fun handle(packet: JEICompatSyncPacket) {
        syncedPlayers.put(packet.playerId, Unit)
    }
}