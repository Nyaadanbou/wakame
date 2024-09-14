package cc.mewcraft.wakame.shadow.network

import me.lucko.shadow.Field
import me.lucko.shadow.Shadow
import me.lucko.shadow.Target
import me.lucko.shadow.bukkit.NmsClassTarget
import net.minecraft.network.syncher.SynchedEntityData

@NmsClassTarget("network.syncher.SynchedEntityData")
internal interface ShadowSynchedEntityData : Shadow {
    @get:Field
    @get:Target("itemById")
    val itemById: Array<SynchedEntityData.DataItem<*>>
}