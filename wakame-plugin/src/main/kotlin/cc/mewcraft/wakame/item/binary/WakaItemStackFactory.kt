package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.util.isNmsObjectBacked
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object WakaItemStackFactory {
    private val cache: LoadingCache<ItemStack, WakaItemStack> = Caffeine.newBuilder()
        .maximumSize(1000)
        .weakKeys()
        .build { key: ItemStack -> WakaItemStackImpl(key) }

    /**
     * Gets a [WakaItemStack] wrapper for the [craftItemStack]. Then, you can
     * use the returned [WakaItemStack] wrapper to directly read/modify the
     * item in the underlying game world.
     *
     * @throws IllegalArgumentException if the [craftItemStack] instance is not
     *     backed by an NMS object
     */
    fun wrap(craftItemStack: ItemStack): WakaItemStack {
        require(craftItemStack.isNmsObjectBacked) { "Can't wrap a non NMS-backed ItemStack as WakaItemStack" }
        return WakaItemStackImpl(craftItemStack)
    }

    /**
     * This function is meant to be used to create an **one-off**
     * [WakaItemStack] and then immediately add it to the underlying game
     * world (such as adding it to a player's inventory and dropping it on
     * the ground). Once the [WakaItemStack] is added to the game world, any
     * changes to it [WakaItemStack] **will not** reflect to that one in the
     * game world. **In no way should you store the returned instance.**
     *
     * If you want to modify the [WakaItemStack]s that are already in the game
     * world (such as modifying the item in a player's inventory), use the
     * function [wrap] instead.
     */
    fun new(material: Material): WakaItemStack {
        return WakaItemStackImpl(material)
    }
}