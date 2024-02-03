package cc.mewcraft.wakame.shadow.inventory

import me.lucko.shadow.Field
import me.lucko.shadow.Shadow
import me.lucko.shadow.bukkit.ObcClassTarget

@ObcClassTarget("inventory.CraftItemStack")
interface ShadowCraftItemStack : Shadow {
    @Field
    fun getHandle(): Any?
}