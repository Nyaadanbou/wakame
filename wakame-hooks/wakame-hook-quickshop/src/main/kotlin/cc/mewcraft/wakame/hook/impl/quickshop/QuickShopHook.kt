package cc.mewcraft.wakame.hook.impl.quickshop

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.hasProperty
import cc.mewcraft.wakame.item2.isExactKoish
import cc.mewcraft.wakame.item2.isKoish
import cc.mewcraft.wakame.item2.koishTypeId
import cc.mewcraft.wakame.util.registerEvents
import com.ghostchu.quickshop.api.event.QSCancellable
import com.ghostchu.quickshop.api.event.display.ItemPreviewComponentPrePopulateEvent
import com.ghostchu.quickshop.api.event.general.ShopItemMatchEvent
import com.ghostchu.quickshop.api.event.management.ShopCreateEvent
import com.ghostchu.quickshop.api.event.settings.type.ShopItemEvent
import com.ghostchu.quickshop.api.event.settings.type.ShopTypeEvent
import com.ghostchu.quickshop.api.shop.Shop
import com.ghostchu.quickshop.api.shop.ShopType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

@Hook(plugins = ["QuickShop-Hikari"])
object QuickShopHook : Listener {
    init {
        registerEvents()
    }

    // 如果 original 属于 Koish 物品类型, 则尝试匹配 Koish 物品类型
    @EventHandler
    fun on(event: ShopItemMatchEvent) {
        LOGGER.info("$event")
        val original = event.original()
        val comparison = event.comparison()
        if (original.isExactKoish && original.koishTypeId == comparison.koishTypeId) {
            event.matches(true)
        }
    }

    // 在玩家创建[收购类型]的商店时, 如果物品类型不允许收购, 则阻止商店创建
    @EventHandler
    fun on(event: ShopCreateEvent) {
        LOGGER.info("$event")
        if (event.phase().cancellable()) {
            tryCancel(event.shop().get(), event)
        }
    }

    // 在玩家更新[收购类型]的商店时, 如果物品类型不允许收购, 则阻止商店更新
    @EventHandler
    fun on(event: ShopTypeEvent) {
        LOGGER.info("$event")
        if (event.phase().cancellable()) {
            tryCancel(event.shop(), event)
        }
    }

    // 在玩家更新[收购类型]的商店时, 如果物品类型不允许收购, 则阻止商店更新
    @EventHandler
    fun on(event: ShopItemEvent) {
        LOGGER.info("$event")
        if (event.phase().cancellable()) {
            tryCancel(event.shop(), event)
        }
    }

    @EventHandler
    fun on(event: ItemPreviewComponentPrePopulateEvent) {
        LOGGER.info("$event")
    }

    private fun tryCancel(shop: Shop, event: QSCancellable) {
        if (shop.shopType == ShopType.BUYING) {
            val shopItem = shop.item
            if (shopItem.isKoish && !shopItem.hasProperty(ItemPropertyTypes.PLAYER_PURCHASABLE)) {
                event.setCancelled(true, TranslatableMessages.MSG_MSG_ITEM_NOT_PURCHASABLE.build())
            }
        }
    }
}