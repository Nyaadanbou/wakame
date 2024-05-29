package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.display.ItemRenderer
import cc.mewcraft.wakame.display.PACKET_ITEM_RENDERER
import cc.mewcraft.wakame.packet.PacketSupport.handleMerchantOffers
import cc.mewcraft.wakame.packet.PacketSupport.handleSetSlot
import cc.mewcraft.wakame.packet.PacketSupport.handleWindowItems
import cc.mewcraft.wakame.util.bukkitPlayer
import cc.mewcraft.wakame.util.packetevents.takeUnlessEmpty
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.User
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMerchantOffers
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems
import org.bukkit.GameMode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import kotlin.jvm.optionals.getOrNull

internal class PacketNekoStackRenderListener : PacketListenerAbstract() {

    override fun onPacketSend(event: PacketSendEvent) {
        val user = event.user
        val newPacket = when (event.packetType) {
            PacketType.Play.Server.SET_SLOT -> handleSetSlot(user, WrapperPlayServerSetSlot(event))
            PacketType.Play.Server.WINDOW_ITEMS -> handleWindowItems(user, WrapperPlayServerWindowItems(event))
            PacketType.Play.Server.MERCHANT_OFFERS -> handleMerchantOffers(user, WrapperPlayServerMerchantOffers(event))
            else -> return
        }
        if (newPacket != null) {
            user.sendPacketSilently(newPacket)
            event.isCancelled = true
        }
    }

}

private object PacketSupport : KoinComponent {
    private val renderer: ItemRenderer<PacketNekoStack> by inject(named(PACKET_ITEM_RENDERER))

    fun handleSetSlot(user: User, packet: WrapperPlayServerSetSlot): PacketWrapper<*>? {
        val item = packet.item.takeUnlessEmpty() ?: return null
        val nekoStack = item.packetNekoStackOrNull ?: return null
        updateItem(nekoStack, user)
        return WrapperPlayServerSetSlot(
            packet.windowId,
            packet.stateId,
            packet.slot,
            nekoStack.itemStack
        )
    }

    fun handleWindowItems(user: User, packet: WrapperPlayServerWindowItems): PacketWrapper<*> {
        val items = packet.items
        val newItems = items.map mapItems@{ item ->
            val nekoStack = item.takeUnlessEmpty()?.packetNekoStackOrNull ?: return@mapItems item
            updateItem(nekoStack, user)
            nekoStack.itemStack
        }

        return WrapperPlayServerWindowItems(
            packet.windowId,
            packet.stateId,
            newItems,
            packet.carriedItem.getOrNull()
        )
    }

    fun handleMerchantOffers(user: User, packet: WrapperPlayServerMerchantOffers): PacketWrapper<*> {
        val offers = packet.merchantOffers
        val newOffers = offers.map mapItems@{ offer ->
            val result = offer.outputItem.takeUnlessEmpty() ?: return@mapItems offer
            val nekoStack = result.packetNekoStackOrNull ?: return@mapItems offer
            updateItem(nekoStack, user)
            offer
        }

        return WrapperPlayServerMerchantOffers(
            packet.containerId,
            newOffers,
            packet.villagerLevel,
            packet.villagerXp,
            packet.isShowProgress,
            packet.isCanRestock
        )
    }

    private fun updateItem(nekoStack: PacketNekoStack, user: User) {
        // TODO: 有需要可做创造模式兼容等
        val player = user.bukkitPlayer
        if (player.gameMode == GameMode.CREATIVE)
            return
        renderer.render(nekoStack)
    }
}
