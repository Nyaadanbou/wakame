package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.util.isNmsObjectBacked
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object NekoItemStackFactory {
    private val cache: LoadingCache<ItemStack, NekoItemStack> = Caffeine.newBuilder()
        .maximumSize(1000)
        .weakKeys()
        .build { key: ItemStack -> NekoItemStackImpl(key) }

    /**
     * Gets a [NekoItemStack] wrapper for the [craftItemStack]. Then, you can
     * use the returned [NekoItemStack] wrapper to directly read/modify the
     * item in the underlying game world.
     *
     * @throws IllegalArgumentException if the [craftItemStack] instance is not
     *     backed by an NMS object
     */
    fun wrap(craftItemStack: ItemStack): NekoItemStack {
        require(craftItemStack.isNmsObjectBacked) { "Can't wrap a non NMS-backed ItemStack as NekoItemStack" }
        return NekoItemStackImpl(craftItemStack)
    }

    /**
     * This function is meant to be used to create an **one-off**
     * [NekoItemStack] and then immediately add it to the underlying game
     * world (such as adding it to a player's inventory and dropping it on
     * the ground). Once the [NekoItemStack] is added to the game world, any
     * changes to it **will not** reflect to that one in the game world.
     *
     * If you want to modify the [NekoItemStack]s that are already in the game
     * world (such as modifying the item in a player's inventory), use the
     * function [wrap] instead.
     */
    fun new(material: Material): NekoItemStack {
        return NekoItemStackImpl(material)
    }
}