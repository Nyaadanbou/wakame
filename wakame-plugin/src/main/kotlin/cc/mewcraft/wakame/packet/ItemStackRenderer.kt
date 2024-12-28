package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.standard.StandardContext
import cc.mewcraft.wakame.initializer.Initializer
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMerchantOffers
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetCursorItem
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import kotlin.jvm.optionals.getOrNull

/**
 * 修改 [org.bukkit.inventory.ItemStack].
 */
internal class ItemStackRenderer : PacketListenerAbstract(), KoinComponent {
    private val logger: Logger by inject()

    override fun onPacketSend(event: PacketSendEvent) {
        // 不修改发给创造模式玩家的物品包
        if (isCreative(event))
            return

        // 修改发给非创造模式玩家的物品包
        val changed = when (event.packetType) {
            PacketType.Play.Server.SET_SLOT -> handleSetSlot(event)
            PacketType.Play.Server.SET_CURSOR_ITEM -> handleSetCursorItem(event)
            PacketType.Play.Server.WINDOW_ITEMS -> handleWindowItems(event)
            PacketType.Play.Server.ENTITY_METADATA -> handleEntityData(event)
            PacketType.Play.Server.ENTITY_EQUIPMENT -> handleSetEquipment(event)
            PacketType.Play.Server.MERCHANT_OFFERS -> handleMerchantOffers(event)
            else -> false
        }
        if (changed) {
            event.markForReEncode(true)
        }
    }

    private fun handleSetSlot(event: PacketSendEvent): Boolean {
        val wrapper = WrapperPlayServerSetSlot(event)
        var changed = wrapper.item.modify()
        return changed
    }

    private fun handleSetCursorItem(event: PacketSendEvent): Boolean {
        val wrapper = WrapperPlayServerSetCursorItem(event)
        var changed = wrapper.stack.modify()
        // 此包会在玩家拿起物品, 服务端内部进行物品纠错时发送,
        // 纠错指的是服务端会检查客户端物品是否与服务端一致, 如果不一致则会将物品设置为服务端的物品.
        // 由于客户端收到的 Wakame 物品与服务端不一致,
        // 因此会导致客户端物品栏内的 Wakame 有时会被错误地设置到鼠标上.
        if (changed) {
            event.isCancelled = true
        }
        return changed
    }

    private fun handleSetEquipment(event: PacketSendEvent): Boolean {
        val wrapper = WrapperPlayServerEntityEquipment(event)
        var changed = false

        for (equipment in wrapper.equipment) {
            changed = equipment.item.modify() || changed
        }

        return changed
    }

    private fun handleWindowItems(event: PacketSendEvent): Boolean {
        val wrapper = WrapperPlayServerWindowItems(event)
        var changed = false

        for (item in wrapper.items) {
            changed = item.modify() || changed
        }

        val carriedItem = wrapper.carriedItem.getOrNull()?.modify()
        changed = changed || (carriedItem == true)

        return changed
    }

    private fun handleMerchantOffers(event: PacketSendEvent): Boolean {
        val wrapper = WrapperPlayServerMerchantOffers(event)
        var changed = false

        for (offer in wrapper.merchantOffers) {
            changed = offer.firstInputItem.modify() || changed
            changed = (offer.secondInputItem?.modify() == true) || changed
            changed = offer.outputItem.modify() || changed
        }

        return changed
    }

    private fun handleEntityData(event: PacketSendEvent): Boolean {
        val wrapper = WrapperPlayServerEntityMetadata(event)
        var changed = false

        for (metadata in wrapper.entityMetadata) {
            val value = metadata.value
            if (value !is ItemStack) {
                continue
            }
            changed = value.modify() || changed
        }

        return changed
    }

    private fun isCreative(event: PacketSendEvent): Boolean {
        // 创造模式会1:1复制它接收到的物品到客户端本地,
        // 而我们发给客户端的萌芽物品并不是原始物品, 而是修改过的.
        // 问题在于, 修改过的萌芽物品并不包含任何 wakame 数据,
        // 这也就导致了创造模式会让物品栏内的萌芽物品失效.
        //
        // 这个问题的解决办法就是在物品上永远存一份原始数据的备份,
        // 但那样会导致额外的内存和性能开销. 不如等 Mojang 更新.
        //
        // 因此, 我们现阶段能做的就是忽略该问题.

        return event.getPlayer<Player>()?.gameMode == GameMode.CREATIVE
    }

    /**
     * @return 如果物品发生了变化则返回 `true`, 否则返回 `false`
     */
    private fun ItemStack.modify(): Boolean {
        var changed: Boolean

        // 移除任意物品的 PDC
        changed = customData?.removeTag("PublicBukkitValues") != null

        val nekoStack = tryNekoStack
        if (nekoStack != null) {
            try {
                ItemRenderers.STANDARD.render(nekoStack, StandardContext)
                changed = true
            } catch (e: Throwable) {
                if (Initializer.isDebug) {
                    e.printStackTrace()
                } else {
                    logger.error("An error occurred while rendering network item: ${nekoStack.id}")
                }
            }
        }

        return changed
    }
}
