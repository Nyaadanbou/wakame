package cc.mewcraft.wakame.messaging

import cc.mewcraft.messaging2.MessagingConfig
import cc.mewcraft.messaging2.MessagingManager
import cc.mewcraft.messaging2.StaticAccessApi
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.messaging.packet.NationSpawnRequestPacket
import cc.mewcraft.wakame.messaging.packet.NationSpawnResponsePacket
import cc.mewcraft.wakame.messaging.packet.TownSpawnRequestPacket
import cc.mewcraft.wakame.messaging.packet.TownSpawnResponsePacket
import ninja.egg82.messenger.handler.AbstractMessagingHandler
import org.slf4j.Logger

class KoishMessagingManager(
    config: MessagingConfig,
) : MessagingManager(config) {

    companion object : StaticAccessApi by StaticAccessApi.of(::KoishMessagingManager)

    override val logger: Logger = LOGGER
    override val channelName: String = "koish:main"
    override val protocolVersion: Byte = 1

    override fun registerPackets() {
        registerPacket(::TownSpawnRequestPacket)
        registerPacket(::TownSpawnResponsePacket)
        registerPacket(::NationSpawnRequestPacket)
        registerPacket(::NationSpawnResponsePacket)
    }

    override fun registerMessagingHandlers(addHandler: (AbstractMessagingHandler) -> Unit) {
        addHandler(KoishMessagingHandler(packetService))
    }
}