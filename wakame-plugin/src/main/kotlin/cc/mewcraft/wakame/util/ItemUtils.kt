package cc.mewcraft.wakame.util

import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.shadow.inventory.ShadowCraftMetaItem
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.shadow
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

val ItemMeta.unhandledTags: MutableMap<String, Tag>
    get() = BukkitShadowFactory.global().shadow<ShadowCraftMetaItem>(this).unhandledTags()

/**
 * Gets the custom model data or `0`, if it does not exist.
 */
val ItemStack.customModelData: Int
    get() {
        if (hasItemMeta()) {
            val itemMeta = itemMeta!!
            if (itemMeta.hasCustomModelData()) return itemMeta.customModelData
        }

        return 0
    }

/**
 * Gets the custom name or an empty text, if the display name does not
 * exist.
 */
val ItemStack.adventureCustomName: Component
    get() {
        if (this.hasItemMeta()) {
            return this.itemMeta.displayName() ?: Component.empty()
        }
        return Component.empty()
    }

/**
 * Gets the item name or an empty text, if the item name does not
 * exist.
 */
val ItemStack.adventureItemName: Component
    get() {
        if (this.hasItemMeta()) {
            return this.itemMeta.itemName() ?: Component.empty()
        }
        return Component.empty()
    }

/**
 * Get the lore or an empty list, if the lore does not exist.
 */
val ItemStack.adventureLore: List<Component>
    get() {
        if (this.hasItemMeta()) {
            return this.itemMeta.lore() ?: emptyList()
        }
        return emptyList()
    }
