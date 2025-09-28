package cc.mewcraft.wakame.hook.impl.quickshop

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item.hasProp
import cc.mewcraft.wakame.item.isExactKoish
import cc.mewcraft.wakame.item.koishTypeId
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.util.registerEvents
import com.ghostchu.quickshop.api.event.QSCancellable
import com.ghostchu.quickshop.api.event.general.ShopItemMatchEvent
import com.ghostchu.quickshop.api.event.settings.type.ShopItemEvent
import com.ghostchu.quickshop.api.event.settings.type.ShopTypeEvent
import com.ghostchu.quickshop.api.shop.Shop
import com.ghostchu.quickshop.api.shop.ShopType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

@Hook(plugins = ["QuickShop-Hikari"])
object QuickShopHook : Listener {
    init {
        registerEvents()
    }

    // 如果 original 属于 Koish 物品类型, 则尝试匹配 Koish 物品类型
    @EventHandler
    fun on(event: ShopItemMatchEvent) {
        val original = event.original()
        val comparison = event.comparison()
        if (original.isExactKoish && original.koishTypeId == comparison.koishTypeId) {
            event.matches(true)
        }
    }

    // 在玩家更新[收购类型]的商店时, 如果物品类型不允许收购, 则阻止商店更新
    @EventHandler
    fun on(event: ShopTypeEvent) {
        if (event.phase().cancellable()) {
            if (event.updated() == ShopType.BUYING) {
                val shop = event.shop()
                val shopItem = shop.item
                tryCancel(shop, shopItem, event)
            }
        }
    }

    // 在玩家更新[收购类型]的商店时, 如果物品类型不允许收购, 则阻止商店更新
    @EventHandler
    fun on(event: ShopItemEvent) {
        if (event.phase().cancellable()) {
            val shop = event.shop()
            if (shop.shopType == ShopType.BUYING) {
                val shopItem = event.updated()
                tryCancel(shop, shopItem, event)
            }
        }
    }

    private fun tryCancel(shop: Shop, shopItem: ItemStack, event: QSCancellable) {
        if (shopItem.isExactKoish && !shopItem.hasProp(ItemPropTypes.PLAYER_PURCHASABLE)) {
            event.setCancelled(true, TranslatableMessages.MSG_ITEM_NOT_PURCHASABLE.build())
            shop.permissionAudiences.keys.mapNotNull { SERVER.getPlayer(it) }.forEach { player ->
                // 通知所有在线的商店管理者
                player.sendMessage(TranslatableMessages.MSG_ITEM_NOT_PURCHASABLE.build())
            }
        }
    }
}