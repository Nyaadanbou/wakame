package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.util.isNmsObjectBacked
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object NekoStackFactory {
    /**
     * The same as [NekoStackFactory.wrap] but it will return `null` if the
     * [itemStack] is not a legal neko item.
     *
     * @throws IllegalArgumentException if the [itemStack] instance is not
     *     backed by an NMS object
     */
    fun by(itemStack: ItemStack): NekoStack? {
        return wrap(itemStack).takeIf { it.isNeko }
    }

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
     * [NekoStack] which will immediately be added to the underlying world
     * state (such as adding it to a player's inventory and dropping it on
     * the ground). Once the [NekoStack] is added to the world state, any
     * changes to the returned NekoStack **will not** reflect to that one
     * in the world state.
     *
     * If you want to modify the [NekoStack]s that are already in the world
     * state (such as modifying the item in a player's inventory), use the
     * functions [by] or [wrap] instead.
     */
    fun new(material: Material): NekoStack {
        return NekoStackImpl(material)
    }
}