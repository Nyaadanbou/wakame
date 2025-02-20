package cc.mewcraft.wakame.network

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.network.event.PacketEvent
import cc.mewcraft.wakame.network.event.PacketHandler
import cc.mewcraft.wakame.network.event.PacketListener
import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundContainerSetContentPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundContainerSetSlotPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundMerchantOffersPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundSetEntityDataPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundSetEquipmentPacketEvent
import cc.mewcraft.wakame.network.event.registerPacketListener
import cc.mewcraft.wakame.network.event.unregisterPacketListener
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import org.bukkit.GameMode
import kotlin.jvm.optionals.getOrNull

private val LOGGING by MAIN_CONFIG.entry<Boolean>("debug", "logging", "renderer")

/**
 * 修改 [org.bukkit.inventory.ItemStack].
 */
@Init(stage = InitStage.POST_WORLD)
internal object ItemStackRenderer : PacketListener {
    private const val PROCESSED_KEY = "processed"

    @InitFun
    private fun init() {
        registerPacketListener()
    }

    @DisableFun
    private fun disable() {
        unregisterPacketListener()
    }

    @PacketHandler
    private fun handleSetSlot(event: ClientboundContainerSetSlotPacketEvent) {
        if (isCreative(event))
            return
        event.item.modify(event)
    }

    @PacketHandler
    private fun handleSetEquipment(event: ClientboundSetEquipmentPacketEvent) {
        if (isCreative(event))
            return

        for (equipment in event.slots) {
            equipment.second.modify(event)
        }
    }

    @PacketHandler
    private fun handleContainerSetContent(event: ClientboundContainerSetContentPacketEvent) {
        for (item in event.items) {
            item.modify(event)
        }
        event.carriedItem.modify(event)
    }

    @PacketHandler
    private fun handleMerchantOffers(event: ClientboundMerchantOffersPacketEvent) {
        if (isCreative(event))
            return
        for (offer in event.offers) {
            offer.itemCostA.itemStack.modify(event)
            offer.itemCostB.getOrNull()?.itemStack?.modify(event)
            offer.result.modify(event)
        }
    }

    @PacketHandler
    private fun handleEntityData(event: ClientboundSetEntityDataPacketEvent) {
        for (metadata in event.packedItems) {
            val value = metadata.value
            if (value !is ItemStack) {
                continue
            }
            value.modify(event)
        }
    }

    private fun isCreative(event: PlayerPacketEvent<*>): Boolean {
        // 创造模式会1:1复制它接收到的物品到客户端本地,
        // 而我们发给客户端的萌芽物品并不是原始物品, 而是修改过的.
        // 问题在于, 修改过的萌芽物品并不包含任何 wakame 数据,
        // 这也就导致了创造模式会让物品栏内的萌芽物品失效.
        //
        // 这个问题的解决办法就是在物品上永远存一份原始数据的备份,
        // 但那样会导致额外的内存和性能开销. 不如等 Mojang 更新.
        //
        // 因此, 我们现阶段能做的就是忽略该问题.

        return event.player.gameMode == GameMode.CREATIVE
    }

    /**
     * @return 如果物品发生了变化则修改 [PacketEvent.changed]
     */
    private fun ItemStack.modify(event: PacketEvent<*>) {
        var changed: Boolean

        // 移除任意物品的 PDC
        changed = get(DataComponents.CUSTOM_DATA)?.update { it.remove("PublicBukkitValues") } != null

        val nekoStack = asBukkitMirror().shadowNeko(false)
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

        event.changed = changed
    }

    private var ItemStack.processed: Boolean
        get() = get(DataComponents.CUSTOM_DATA)?.contains(PROCESSED_KEY) == true
        set(value) {
            if (value) {
                val customData = getOrDefault(DataComponents.CUSTOM_DATA, CustomData.of(CompoundTag()))
                val newCustomData = customData.update { it.put(PROCESSED_KEY, ByteTag.valueOf(true)) }
                set(DataComponents.CUSTOM_DATA, newCustomData)
            } else {
                val customData = get(DataComponents.CUSTOM_DATA)
                if (customData != null) {
                    val newCustomData = customData.update { it.remove(PROCESSED_KEY) }
                    set(DataComponents.CUSTOM_DATA, newCustomData)
                }
            }
        }
}
