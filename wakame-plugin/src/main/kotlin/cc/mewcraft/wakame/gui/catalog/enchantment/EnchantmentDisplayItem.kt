package cc.mewcraft.wakame.gui.catalog.enchantment

import cc.mewcraft.wakame.catalog.enchantment.CatalogEnchantmentEntry
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper

private val MM = MiniMessage.miniMessage()
private const val MAX_DISPLAY_ITEMS = 8

internal fun buildEnchantmentItem(entry: CatalogEnchantmentEntry): Item {
    return Item.builder()
        .setItemProvider { _ ->
            val itemRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM)

            val loreLines = mutableListOf<Component>()

            loreLines.add(MM.deserialize("<gray>最大等级: <white>${entry.maxLevel}"))

            // Supported items
            val supportedKeys = entry.supportedItems.toList()
            if (supportedKeys.isNotEmpty()) {
                val displayed = supportedKeys.take(MAX_DISPLAY_ITEMS).map { key ->
                    val itemType = itemRegistry.get(key)
                    if (itemType != null) Component.translatable(itemType)
                    else Component.text(key.key().value())
                }
                val joined = Component.join(
                    JoinConfiguration.commas(true),
                    displayed
                ).colorIfAbsent(NamedTextColor.WHITE)
                val more = if (supportedKeys.size > MAX_DISPLAY_ITEMS) {
                    MM.deserialize("<gray> 等 ${supportedKeys.size} 种")
                } else {
                    Component.empty()
                }
                loreLines.add(
                    Component.textOfChildren(
                        MM.deserialize("<gray>支持的物品: "),
                        joined,
                        more
                    )
                )
            }

            // Primary items (only if present)
            val primaryKeys = entry.primaryItems?.toList()
            if (!primaryKeys.isNullOrEmpty()) {
                val displayed = primaryKeys.take(MAX_DISPLAY_ITEMS).map { key ->
                    val itemType = itemRegistry.get(key)
                    if (itemType != null) Component.translatable(itemType)
                    else Component.text(key.key().value())
                }
                val joined = Component.join(
                    JoinConfiguration.commas(true),
                    displayed
                ).colorIfAbsent(NamedTextColor.WHITE)
                val more = if (primaryKeys.size > MAX_DISPLAY_ITEMS) {
                    MM.deserialize("<gray> 等 ${primaryKeys.size} 种")
                } else {
                    Component.empty()
                }
                loreLines.add(
                    Component.textOfChildren(
                        MM.deserialize("<gray>主要的物品: "),
                        joined,
                        more
                    )
                )
            }

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
