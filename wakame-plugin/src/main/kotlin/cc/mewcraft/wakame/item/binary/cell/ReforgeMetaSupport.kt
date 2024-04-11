package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.item.ReforgeBinaryKeys
import cc.mewcraft.wakame.util.CompoundShadowTag
import cc.mewcraft.wakame.util.toStableByte
import me.lucko.helper.shadows.nbt.ShadowTag

/**
 * 代表一个空的重铸元数据。
 */
data object EmptyReforgeData : ReforgeData {
    override val successCount: Int get() = throw UnsupportedOperationException("EmptyReforgeData has no success count")
    override val failureCount: Int get() = throw UnsupportedOperationException("EmptyReforgeData has no success count")
    override fun asShadowTag(): ShadowTag = me.lucko.helper.shadows.nbt.CompoundShadowTag.create()
}

/**
 * 代表一个不可变的重铸元数据。
 */
data class ImmutableReforgeData(
    override val successCount: Int,
    override val failureCount: Int,
) : ReforgeData {
    override fun asShadowTag(): ShadowTag {
        return CompoundShadowTag {
            putByte(ReforgeBinaryKeys.SUCCESS_COUNT, successCount.toStableByte())
            putByte(ReforgeBinaryKeys.FAILURE_COUNT, failureCount.toStableByte())
        }
    }
}
