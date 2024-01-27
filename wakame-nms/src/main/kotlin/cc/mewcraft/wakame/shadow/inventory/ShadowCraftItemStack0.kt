package cc.mewcraft.wakame.shadow.inventory

import me.lucko.shadow.Field
import me.lucko.shadow.Shadow
import me.lucko.shadow.bukkit.ObcClassTarget
import net.minecraft.world.item.ItemStack as MojangStack

@ObcClassTarget("inventory.CraftItemStack")
interface ShadowCraftItemStack0 : Shadow {
    @Field
    fun getHandle(): MojangStack?
}