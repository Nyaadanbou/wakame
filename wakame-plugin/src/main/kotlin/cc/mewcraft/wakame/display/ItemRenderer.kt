package cc.mewcraft.wakame.display

import org.bukkit.inventory.ItemStack

/**
 * A renderer that generates name and lore for an item.
 */
interface ItemRenderer {
    /**
     * Renders the [itemStack] in-place.
     *
     * @param itemStack the item to be rendered
     */
    fun render(itemStack: ItemStack)
}