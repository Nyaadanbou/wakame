package cc.mewcraft.extracontexts.common.messaging

import cc.mewcraft.extracontexts.common.messaging.packet.CacheInvalidationPacket
import cc.mewcraft.messaging2.AbstractMessagingManager
import cc.mewcraft.messaging2.MessagingConfiguration
import cc.mewcraft.messaging2.StaticAccessApi
import ninja.egg82.messenger.handler.AbstractMessagingHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MessagingManager(
    config: MessagingConfiguration,
) : AbstractMessagingManager(config) {

    companion object : StaticAccessApi by StaticAccessApi.of(::MessagingManager)

    override val logger: Logger = LoggerFactory.getLogger("ExtraContexts")
    override val channelName: String = "extracontexts:main"
    override val protocolVersion: Byte = 1

    override fun registerPackets() {
        registerPacket(::CacheInvalidationPacket)
    }

    override fun registerMessagingHandlers(addHandler: (AbstractMessagingHandler) -> Unit) {
        addHandler(MessagingHandler(packetService))
    }
}