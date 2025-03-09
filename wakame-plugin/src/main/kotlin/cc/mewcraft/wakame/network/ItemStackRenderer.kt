package cc.mewcraft.wakame.network

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.item.wrap
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.network.event.*
import cc.mewcraft.wakame.network.event.PacketHandler
import cc.mewcraft.wakame.network.event.clientbound.*
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.editNbt
import cc.mewcraft.wakame.util.item.fastUpdate
import cc.mewcraft.wakame.util.item.isNetworkRewrite
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.CustomData
import org.bukkit.GameMode
import kotlin.jvm.optionals.getOrNull

private val LOGGING by MAIN_CONFIG.entry<Boolean>("debug", "logging", "renderer")

/**
 * 修改 [net.minecraft.world.item.ItemStack].
 */
@Init(stage = InitStage.POST_WORLD)
internal object ItemStackRenderer : PacketListener {

    @InitFun
    fun init() {
        registerPacketListener()
    }

    @DisableFun
    fun disable() {
        unregisterPacketListener()
    }

    @PacketHandler
    private fun handleSetSlot(event: ClientboundContainerSetSlotPacketEvent) {
        if (isCreative(event)) return
        event.item.modify(event)
    }

    @PacketHandler
    private fun handleSetEquipment(event: ClientboundSetEquipmentPacketEvent) {
        if (isCreative(event)) return
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
        if (isCreative(event)) return
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
            if (value !is MojangStack) continue
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

    private const val PDC_FIELD = "PublicBukkitValues"

    private fun MojangStack.modify(event: PacketEvent<*>) {
        var changed = false

        // 移除任意物品的 PDC
        editNbt { nbt ->
            if (nbt.contains(PDC_FIELD)) {
                changed = true
                processed = true
            }
            nbt.remove(PDC_FIELD)
        }

        val koishStack = wrap()
        if (koishStack != null && isNetworkRewrite) {
            try {
                ItemRenderers.STANDARD.render(koishStack)
                changed = true
            } catch (e: Throwable) {
                if (LOGGING) {
                    LOGGER.error("An error occurred while rewrite network item: ${koishStack.id}", e)
                }
            }
        }

        event.changed = changed
    }

    private const val PROCESSED_FIELD = "processed"

    private var MojangStack.processed: Boolean
        get() = get(DataComponents.CUSTOM_DATA)?.contains(PROCESSED_FIELD) == true
        set(value) {
            fastUpdate(
                type = DataComponents.CUSTOM_DATA,
                default = { CustomData.of(CompoundTag()) },
                applier = { customData ->
                    customData.update { nbt ->
                        if (value) {
                            nbt.put(PROCESSED_FIELD, ByteTag.ZERO)
                        } else {
                            nbt.remove(PROCESSED_FIELD)
                        }
                    }
                }
            )
        }
}
