package cc.mewcraft.wakame.shadow.inventory

import me.lucko.shadow.Field
import me.lucko.shadow.Shadow
import me.lucko.shadow.bukkit.ObcClassTarget
import net.minecraft.nbt.Tag

@ObcClassTarget("inventory.CraftMetaItem")
interface ShadowCraftMetaItem0 : Shadow {
    @Field
    fun getUnhandledTags(): MutableMap<String, Tag>
}