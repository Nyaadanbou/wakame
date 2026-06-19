package cc.mewcraft.wakame.gui.catalog.enchantment

import cc.mewcraft.wakame.catalog.enchantment.CatalogEnchantmentEntry
import cc.mewcraft.wakame.catalog.enchantment.EnchantmentItemResolver
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper

/**
 * 魔咒图鉴展示层的物品构建工具.
 *
 * 从 [CatalogEnchantmentEntry] 构建用于 GUI 展示的附魔书 [Item].
 * 与物品图鉴的 [cc.mewcraft.wakame.gui.catalog.item.CatalogItemDisplay] 对齐.
 */
internal object CatalogEnchantmentDisplay {

    private val MM = MiniMessage.miniMessage()
    private const val MAX_DISPLAY_ITEMS = 8

    /**
     * 从 [entry] 构建一个用于展示的附魔书 [Item].
     */
    fun buildItem(entry: CatalogEnchantmentEntry): Item {
        return Item.builder()
            .setItemProvider { _ ->
                val loreLines = mutableListOf<Component>()

                loreLines.add(MM.deserialize("<gray>最大等级: <white>${entry.maxLevel}"))

                buildItemListLore("<gray>支持的物品: ", entry.supportedItems)?.let { loreLines.add(it) }
                buildItemListLore("<gray>主要的物品: ", entry.primaryItems)?.let { loreLines.add(it) }

                loreLines.add(MM.deserialize("<gray>权重: <white>${entry.weight}"))

                entry.description?.let { desc ->
                    loreLines.add(Component.empty())
                    loreLines.add(desc)
                }

                val stack = ItemStack.of(Material.ENCHANTED_BOOK)
                stack.setData(DataComponentTypes.ITEM_NAME, entry.displayName)
                stack.setData(DataComponentTypes.LORE, ItemLore.lore(loreLines))

                ItemWrapper(stack)
            }
            .build()
    }

    /**
     * 构建 "支持的物品 / 主要的物品" 这类物品列表 lore 行.
     *
     * 列表为空时返回 `null`; 超过 [MAX_DISPLAY_ITEMS] 时追加 "等 N 种" 后缀.
     */
    private fun buildItemListLore(label: String, items: Collection<EnchantmentItemResolver>): Component? {
        if (items.isEmpty()) return null
        val displayed = items.take(MAX_DISPLAY_ITEMS).map { it.name }
        val joined = Component.join(JoinConfiguration.commas(true), displayed)
            .colorIfAbsent(NamedTextColor.WHITE)
        val more = if (items.size > MAX_DISPLAY_ITEMS) {
            MM.deserialize("<gray> 等 ${items.size} 种")
        } else {
            Component.empty()
        }
        return Component.textOfChildren(MM.deserialize(label), joined, more)
    }
}
