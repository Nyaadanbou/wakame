package cc.mewcraft.wakame.util

import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

/**
 * Checks if `this` [ItemStack] has a backing NMS object.
 */
val ItemStack.isNms: Boolean
    get() {
        return if (this is CraftItemStack) {
            this.handle !== null
        } else {
            false
        }
    }