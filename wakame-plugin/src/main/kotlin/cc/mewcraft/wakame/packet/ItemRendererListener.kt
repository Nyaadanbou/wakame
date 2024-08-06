package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.display.PacketItemRenderer
import cc.mewcraft.wakame.packet.PacketSupport.handleEntityData
import cc.mewcraft.wakame.packet.PacketSupport.handleMerchantOffers
import cc.mewcraft.wakame.packet.PacketSupport.handleSetSlot
import cc.mewcraft.wakame.packet.PacketSupport.handleWindowItems
import cc.mewcraft.wakame.util.bukkitPlayer
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.User
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMerchantOffers
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems
import org.bukkit.GameMode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import kotlin.jvm.optionals.getOrNull

internal class ItemRendererListener : PacketListenerAbstract() {

    override fun onPacketSend(event: PacketSendEvent) {
        val user = event.user
        val newPacket = when (event.packetType) {
            PacketType.Play.Server.SET_SLOT -> handleSetSlot(user, WrapperPlayServerSetSlot(event))
            PacketType.Play.Server.WINDOW_ITEMS -> handleWindowItems(user, WrapperPlayServerWindowItems(event))
            PacketType.Play.Server.MERCHANT_OFFERS -> handleMerchantOffers(user, WrapperPlayServerMerchantOffers(event))
            PacketType.Play.Server.ENTITY_METADATA -> handleEntityData(user, WrapperPlayServerEntityMetadata(event))
            else -> return
        }
        if (newPacket != null) {
            user.sendPacketSilently(newPacket)
            event.isCancelled = true
        }
    }

}

private object PacketSupport : KoinComponent {
    private val logger: Logger by inject()
    private val renderer: PacketItemRenderer by inject()

    fun handleSetSlot(user: User, packet: WrapperPlayServerSetSlot): PacketWrapper<*>? {
        val itemStack = packet.item.takeUnlessEmpty() ?: return null
        val nekoStack = itemStack.tryNekoStack?.takeIf { it.shouldRender } ?: return null
        updateItem(nekoStack, user)
        return WrapperPlayServerSetSlot(
            packet.windowId,
            packet.stateId,
            packet.slot,
            nekoStack.handle0
        )
    }

    fun handleWindowItems(user: User, packet: WrapperPlayServerWindowItems): PacketWrapper<*> {
        val itemStacks = packet.items
        val newItemStacks = itemStacks.map { item ->
            val nekoStack = item.takeUnlessEmpty()?.tryNekoStack?.takeIf { it.shouldRender } ?: return@map item
            updateItem(nekoStack, user)
            nekoStack.handle0
        }

        return WrapperPlayServerWindowItems(
            packet.windowId,
            packet.stateId,
            newItemStacks,
            packet.carriedItem.getOrNull()
        )
    }

    fun handleMerchantOffers(user: User, packet: WrapperPlayServerMerchantOffers): PacketWrapper<*> {
        val offers = packet.merchantOffers
        val newOffers = offers.map mapItems@{ offer ->
            val itemStack = offer.outputItem.takeUnlessEmpty() ?: return@mapItems offer
            val nekoStack = itemStack.tryNekoStack?.takeIf { it.shouldRender } ?: return@mapItems offer
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

    fun handleEntityData(user: User, packet: WrapperPlayServerEntityMetadata): PacketWrapper<*> {
        val oldMetadata = packet.entityMetadata
        val newMetadata = ArrayList<EntityData>()

        for (metadata in oldMetadata) {
            val value = metadata.value
            if (value !is ItemStack) {
                newMetadata += metadata
                continue
            }

            val nekoStack = value.takeUnlessEmpty()?.tryNekoStack?.takeIf { it.shouldRender }
            if (nekoStack == null) {
                newMetadata += metadata
                continue
            }
            updateItem(nekoStack, user)

            newMetadata += EntityData(
                metadata.index,
                EntityDataTypes.ITEMSTACK,
                nekoStack.handle0
            )
        }

        return WrapperPlayServerEntityMetadata(
            packet.entityId,
            newMetadata
        )
    }

    private fun updateItem(nekoStack: PacketNekoStack, user: User) {
        // 创造模式会复制假的物品到客户端本地,
        // 而假的物品是不包含 wakame 数据的,
        // 最终导致物品上的 wakame 数据被清除.
        //
        // 这个问题的解决办法就是在物品上永远存一份原始数据的备份,
        // 但那样会导致额外的内存和性能开销. 不如等 Mojang 更新.
        //
        // 因此, 我们现阶段直接忽视该问题.

        val player = user.bukkitPlayer
        if (player.gameMode == GameMode.CREATIVE) {
            return
        }

        try {
            renderer.render(nekoStack)
        } catch (e: Throwable) {
            logger.error("An error occurred while rendering NekoStack: $nekoStack", e)
        }
    }
}
