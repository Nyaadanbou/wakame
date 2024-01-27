package cc.mewcraft.wakame.shadow.inventory

import me.lucko.shadow.ClassTarget
import me.lucko.shadow.Field
import me.lucko.shadow.Shadow
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

@ClassTarget(ItemStack::class)
interface ShadowItemStack : Shadow {
    @Field
    fun getMeta(): ItemMeta?

    @Field
    fun setMeta(meta: ItemMeta)
}