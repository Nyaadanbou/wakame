package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.util.isNmsObjectBacked
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object NekoStackFactory {
    /**
     * Gets a [NekoStack] representation for the [itemStack]. Then, you can
     * use the returned [NekoStack] object to directly read/modify the
     * ItemStack in the underlying world state.
     *
     * @throws IllegalArgumentException if the [itemStack] instance is not
     *     backed by an NMS object
     */
    fun wrap(itemStack: ItemStack): NekoStack {
        require(itemStack.isNmsObjectBacked) { "Can't wrap a non NMS-backed ItemStack as NekoStack" }
        return NekoStackImpl(itemStack)
    }

    /**
     * This function is meant to be used to create an **one-off**
     * [NekoStack] and then immediately add it to the underlying world
     * state (such as adding it to a player's inventory and dropping it on
     * the ground). Once the [NekoStack] is added to the world state, any
     * changes to it **will not** reflect to that one in the world state.
     *
     * If you want to modify the [NekoStack]s that are already in the world
     * state (such as modifying the item in a player's inventory), use the
     * function [wrap] instead.
     */
    fun new(material: Material): NekoStack {
        return NekoStackImpl(material)
    }
}