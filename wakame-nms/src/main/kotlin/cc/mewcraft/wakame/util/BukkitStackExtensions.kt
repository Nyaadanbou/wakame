package cc.mewcraft.wakame.util

import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

val ItemStack.isNmsObjectBacked: Boolean
    get() {
        return if (this is CraftItemStack) {
            this.handle !== null
        } else {
            false
        }
    }