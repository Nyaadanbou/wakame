package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.display.ItemRenderer
import cc.mewcraft.wakame.item.binary.PacketNekoStack
import cc.mewcraft.wakame.item.binary.packetNekoStackOrNull
import cc.mewcraft.wakame.util.bukkitPlayer
import cc.mewcraft.wakame.util.takeUnlessEmpty
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.User
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMerchantOffers
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems
import org.bukkit.GameMode
import kotlin.jvm.optionals.getOrNull

private fun interface PacketHandler<T : PacketWrapper<T>> {
    fun handle(user: User, packet: T): PacketWrapper<T>?

    operator fun invoke(user: User, packet: T): PacketWrapper<T>? = handle(user, packet)
}

class PacketNekoStackRenderListener(
    private val renderer: ItemRenderer<PacketNekoStack>
) : PacketListenerAbstract() {

    override fun onPacketSend(event: PacketSendEvent) {
        val user = event.user
        val newPacket = when (event.packetType) {
            PacketType.Play.Server.SET_SLOT -> setSlot(user, WrapperPlayServerSetSlot(event))
            PacketType.Play.Server.WINDOW_ITEMS -> windowItems(user, WrapperPlayServerWindowItems(event))
            PacketType.Play.Server.MERCHANT_OFFERS -> merchantOffers(user, WrapperPlayServerMerchantOffers(event))
            else -> return
        }
        if (newPacket != null) {
            user.sendPacketSilently(newPacket)
            event.isCancelled = true
        }
    }

    private fun updateItem(nekoStack: PacketNekoStack, user: User) {
        // TODO: 有需要可做创造模式兼容等
        val player = user.bukkitPlayer
        if (player.gameMode == GameMode.CREATIVE)
            return
        renderer.render(nekoStack)
    }

    /* Packet handling */

    private val setSlot: PacketHandler<WrapperPlayServerSetSlot> =
        PacketHandler { user, packet ->
            val item = packet.item.takeUnlessEmpty() ?: return@PacketHandler null
            val nekoStack = item.packetNekoStackOrNull ?: return@PacketHandler null
            updateItem(nekoStack, user)
            WrapperPlayServerSetSlot(
                packet.windowId,
                packet.stateId,
                packet.slot,
                nekoStack.itemStack
            )
        }

    private val windowItems: PacketHandler<WrapperPlayServerWindowItems> =
        PacketHandler { user, packet ->
            val items = packet.items
            val newItems = items.map mapItems@{ item ->
                val nekoStack = item.takeUnlessEmpty()?.packetNekoStackOrNull ?: return@mapItems item
                updateItem(nekoStack, user)
                nekoStack.itemStack
            }

            WrapperPlayServerWindowItems(
                packet.windowId,
                packet.stateId,
                newItems,
                packet.carriedItem.getOrNull()
            )
        }

    private val merchantOffers: PacketHandler<WrapperPlayServerMerchantOffers> =
        PacketHandler { user, packet ->
            val offers = packet.merchantOffers
            val newOffers = offers.map mapItems@{ offer ->
                val result = offer.outputItem.takeUnlessEmpty() ?: return@mapItems offer
                val nekoStack = result.packetNekoStackOrNull ?: return@mapItems offer
                updateItem(nekoStack, user)
                offer
            }

            WrapperPlayServerMerchantOffers(
                packet.containerId,
                newOffers,
                packet.villagerLevel,
                packet.villagerXp,
                packet.isShowProgress,
                packet.isCanRestock
            )
        }
}