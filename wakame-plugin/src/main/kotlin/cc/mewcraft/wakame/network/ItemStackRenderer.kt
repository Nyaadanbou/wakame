package cc.mewcraft.wakame.network

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display2.ItemRenderers
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.protocol.nbt.NBTByte
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.*
import org.bukkit.GameMode
import org.bukkit.entity.Player
import kotlin.jvm.optionals.getOrNull

private val LOGGING by MAIN_CONFIG.entry<Boolean>("debug", "logging", "renderer")

/**
 * 修改 [org.bukkit.inventory.ItemStack].
 */
internal object ItemStackRenderer : PacketListenerAbstract() {
    private const val PROCESSED_KEY = "processed"

    override fun onPacketSend(event: PacketSendEvent) {
        // 不修改发给创造模式玩家的物品包
        if (isCreative(event))
            return

        // 修改发给非创造模式玩家的物品包
        val changed = when (event.packetType) {
            PacketType.Play.Server.SET_SLOT -> handleSetSlot(event)
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
                ItemRenderers.STANDARD.render(nekoStack)
                processed = true
                changed = true
            } catch (e: Throwable) {
                if (LOGGING) {
                    LOGGER.error("An error occurred while rendering network item: ${nekoStack.id}", e)
                }
            }
        }

        return changed
    }

    private var ItemStack.processed: Boolean
        get() = customData?.getTagOrNull(PROCESSED_KEY) == null
        set(value) {
            if (value) {
                customData?.setTag(PROCESSED_KEY, NBTByte(0))
            } else {
                customData?.removeTag(PROCESSED_KEY)
            }
        }
}
