package cc.mewcraft.wakame.shadow.network.syncher

import me.lucko.shadow.ClassTarget
import me.lucko.shadow.Field
import me.lucko.shadow.Shadow
import me.lucko.shadow.Target
import net.minecraft.network.syncher.SynchedEntityData

@ClassTarget(SynchedEntityData::class)
interface ShadowSynchedEntityData : Shadow {
    @get:Field
    @get:Target("itemsById")
    val itemsById: Array<SynchedEntityData.DataItem<*>>
}