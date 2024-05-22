package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.display.ItemRenderer
import cc.mewcraft.wakame.item.binary.PacketNekoStack
import cc.mewcraft.wakame.util.takeUnlessEmpty
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.User
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot

class PlayNekoStackRenderListener(
    private val renderer: ItemRenderer<PacketNekoStack>
) : PacketListenerAbstract() {
    override fun onPacketSend(event: PacketSendEvent) {
        val user = event.user
        val isSuccess = when (event.packetType) {
            PacketType.Play.Server.SET_SLOT -> handleSetSlot(user, WrapperPlayServerSetSlot(event))
            else -> false
        }
        if (isSuccess) event.isCancelled = true
    }

    private fun handleSetSlot(user: User, packet: WrapperPlayServerSetSlot): Boolean {
        val item = packet.item.takeUnlessEmpty() ?: return false
        val nekoStack = PacketNekoStackImpl(item)
        renderer.render(nekoStack)
        val newPacket = WrapperPlayServerSetSlot(
            packet.windowId,
            packet.stateId,
            packet.slot,
            nekoStack.packetStack
        )

        user.sendPacketSilently(newPacket)
        return true
    }
}