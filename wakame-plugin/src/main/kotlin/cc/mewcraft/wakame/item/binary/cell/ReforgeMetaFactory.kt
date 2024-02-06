package cc.mewcraft.wakame.item.binary.cell

import me.lucko.helper.shadows.nbt.CompoundShadowTag

/**
 * 用来从 NBT 创建 [ReforgeMeta] 的工厂。
 */
object ReforgeMetaFactory {
    fun decode(compoundTag: CompoundShadowTag): ReforgeMeta {
        if (compoundTag.isEmpty) {
            return emptyReforgeMeta()
        }

        return ReforgeMetaImpl(
            successCount = compoundTag.getInt(ReforgeMetaTagNames.SUCCESS),
            failureCount = compoundTag.getInt(ReforgeMetaTagNames.FAILURE)
        )
    }
}