package cc.mewcraft.wakame.shadow.inventory

import me.lucko.shadow.*
import me.lucko.shadow.Target
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

@ClassTarget(ItemStack::class)
internal interface ShadowItemStack : Shadow {
    @get:Field
    @get:Target("craftDelegate")
    val craftDelegate: CraftItemStack
}