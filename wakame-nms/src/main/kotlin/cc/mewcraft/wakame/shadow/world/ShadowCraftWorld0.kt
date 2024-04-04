package cc.mewcraft.wakame.shadow.world

import me.lucko.shadow.Field
import me.lucko.shadow.Shadow
import me.lucko.shadow.bukkit.ObcClassTarget
import net.minecraft.server.level.ServerLevel

@ObcClassTarget("CraftWorld")
interface ShadowCraftWorld0 : Shadow {
    @Field
    fun getWorld(): ServerLevel
}