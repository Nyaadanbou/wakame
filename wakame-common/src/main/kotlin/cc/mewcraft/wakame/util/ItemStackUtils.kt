package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.shadow.inventory.ShadowCraftItemStack
import me.lucko.shadow.bukkit.BukkitShadowFactory
import me.lucko.shadow.shadow
import me.lucko.shadow.targetClass
import org.bukkit.inventory.ItemStack

val ItemStack.isNmsObjectBacked: Boolean
    get() {
        val obcClass = BukkitShadowFactory.global().targetClass<ShadowCraftItemStack>()
        if (obcClass.isInstance(this)) {
            val shadow = BukkitShadowFactory.global().shadow<ShadowCraftItemStack>(this)
            return shadow.getHandle() != null
        }

        return false
    }