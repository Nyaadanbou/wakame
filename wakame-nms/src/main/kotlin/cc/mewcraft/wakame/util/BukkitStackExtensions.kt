package cc.mewcraft.wakame.util

import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

/**
 * Checks if `this` ItemStack is backed by an NMS object.
 *
 * - Returning `true` means the [ItemStack] is backed by an NMS object.
 * - Returning `false` means the [ItemStack] is a strictly-Bukkit [ItemStack].
 *
 * So what's so important about it? It should be noted that the server
 * implementation always makes a **NMS copy** out of a strictly-Bukkit
 * [ItemStack] when the item is being added to the underlying world state.
 *
 * This will lead to the case in which any changes to the strictly-Bukkit
 * ItemStack will not apply to that corresponding NMS ItemStack in the world
 * state, which pretty makes sense as they are different objects!
 *
 * However, this may not hold if the Paper
 * team finish up the ItemStack overhaul:
 * [Interface ItemStacks](https://github.com/orgs/PaperMC/projects/6#).
 * At that time, this property will probably no longer be needed.
 *
 * Please keep an eye on this kdoc. I will add notes to here as soon as
 * anything has changed.
 */
val ItemStack.isNms: Boolean
    get() {
        return if (this is CraftItemStack) {
            this.handle !== null
        } else {
            false
        }
    }