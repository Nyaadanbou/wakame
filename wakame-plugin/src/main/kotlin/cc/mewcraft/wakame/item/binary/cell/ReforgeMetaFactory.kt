package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.NekoTags
import me.lucko.helper.shadows.nbt.CompoundShadowTag

object ReforgeMetaFactory {

    fun decode(compoundTag: CompoundShadowTag): ReforgeMeta {
        if (compoundTag.isEmpty) {
            return emptyReforgeMeta()
        }

        return ReforgeMetaImpl(
            successCount = compoundTag.getInt(NekoTags.Reforge.SUCCESS),
            failureCount = compoundTag.getInt(NekoTags.Reforge.FAILURE)
        )
    }

}