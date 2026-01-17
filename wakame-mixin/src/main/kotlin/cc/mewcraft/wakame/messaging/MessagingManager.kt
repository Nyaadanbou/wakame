package cc.mewcraft.wakame.messaging

import cc.mewcraft.messaging2.AbstractMessagingManager
import cc.mewcraft.messaging2.MessagingConfiguration
import cc.mewcraft.messaging2.StaticAccessApi
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.messaging.packet.*
import ninja.egg82.messenger.handler.AbstractMessagingHandler
import org.slf4j.Logger

class MessagingManager(
    config: MessagingConfiguration,
) : AbstractMessagingManager(config) {

    companion object : StaticAccessApi by StaticAccessApi.of(::MessagingManager)

    override val logger: Logger = LOGGER
    override val channelName: String = "koish:main"
    override val protocolVersion: Byte = 1

    override fun registerPackets() {
        /* TeleportOnJoin */
        registerPacket(::TeleportOnJoinRequestPacket)

        /* TownyNetwork */
        registerPacket(::TownSpawnRequestPacket)
        registerPacket(::TownSpawnResponsePacket)
        registerPacket(::NationSpawnRequestPacket)
        registerPacket(::NationSpawnResponsePacket)
    }

    override fun registerMessagingHandlers(addHandler: (AbstractMessagingHandler) -> Unit) {
        addHandler(MessagingHandler(packetService))
    }
}