package cc.mewcraft.wakame.hook.impl.chestshop

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.item.hasProperty
import cc.mewcraft.wakame.item.property.ItemPropertyTypes
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import cc.mewcraft.wakame.util.registerEvents
import com.Acrobot.Breeze.Utils.StringUtil
import com.Acrobot.ChestShop.Events.ItemParseEvent
import com.Acrobot.ChestShop.Events.ItemStringQueryEvent
import com.Acrobot.ChestShop.Events.PreShopCreationEvent
import com.Acrobot.ChestShop.Signs.ChestShopSign
import net.kyori.adventure.key.Key
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent

@Hook(plugins = ["ChestShop"])
object ChestShopHook : Listener {

    init {
        registerEvents()
    }

    @EventHandler
    fun on(event: ChunkLoadEvent) {
        event.chunk.tileEntities
    }

    // 当试图创建一个商店时, 禁止使用不可收购的 Koish 物品
    @EventHandler(priority = EventPriority.LOW)
    fun on(event: PreShopCreationEvent) {
        LOGGER.info(event.toString())
        val sign = event.sign
        val priceLine = ChestShopSign.getPrice(sign)
        val itemLine = ChestShopSign.getItem(sign)
        val itemId = if (Key.parseable(itemLine)) Key.key(itemLine) else return
        val itemRef = ItemRef.create(itemId) ?: return
        if (itemRef.id.namespace() == KOISH_NAMESPACE) {
            if (priceLine.contains("S", ignoreCase = true)) {
                val koishItem = BuiltInRegistries.ITEM[itemId] ?: return
                val shouldCancel = !koishItem.hasProperty(ItemPropertyTypes.PLAYER_PURCHASABLE)
                event.isCancelled = shouldCancel
            }
        }
    }

    // 当试图将一个物品转换为字符串时, 考虑 Koish 物品
    @EventHandler(priority = EventPriority.LOW)
    fun on(event: ItemStringQueryEvent) {
        LOGGER.info(event.toString())
        val item = event.item
        val itemRef = ItemRef.create(item)
        if (itemRef.id.namespace() != Key.MINECRAFT_NAMESPACE) {
            val code = itemRef.id.toString()
            // Make sure the ItemRef string is not too long as we can't parse shortened ones
            if (event.maxWidth > 0) {
                val width = StringUtil.getMinecraftStringWidth(code)
                if (width > event.maxWidth) {
                    LOGGER.warn("Can't use ItemBridge alias $code as it's width ($width) was wider than the allowed max width of ${event.maxWidth}")
                    return
                }
            }
            event.itemString = code
        }
    }

    // 当试图将一个字符串转换为物品堆叠时, 考虑 Koish 物品
    @EventHandler(priority = EventPriority.LOW)
    fun on(event: ItemParseEvent) {
        LOGGER.info(event.toString())
        val item = event.item
        if (item == null) {
            val itemString = event.itemString
            if (Key.parseable(itemString)) {
                val itemKey = Key.key(itemString)
                val itemRef = ItemRef.create(itemKey)
                if (itemRef != null) {
                    event.item = itemRef.createItemStack()
                }
            }
        }
    }
}