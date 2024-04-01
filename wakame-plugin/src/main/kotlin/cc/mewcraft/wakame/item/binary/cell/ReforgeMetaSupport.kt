package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.util.CompoundShadowTag
import cc.mewcraft.wakame.util.toStableByte
import me.lucko.helper.shadows.nbt.ShadowTag

/**
 * 代表一个空的重铸元数据。
 */
data object EmptyReforgeMeta : ReforgeMeta {
    override val successCount: Int get() = throw UnsupportedOperationException("EmptyReforgeMeta has no success count")
    override val failureCount: Int get() = throw UnsupportedOperationException("EmptyReforgeMeta has no success count")
    override fun asShadowTag(): ShadowTag = me.lucko.helper.shadows.nbt.CompoundShadowTag.create()
}

/**
 * 代表一个不可变的重铸元数据。
 */
data class ImmutableReforgeMeta(
    override val successCount: Int,
    override val failureCount: Int,
) : ReforgeMeta {
    override fun asShadowTag(): ShadowTag {
        return CompoundShadowTag {
            putByte(NekoTags.Reforge.SUCCESS, successCount.toStableByte())
            putByte(NekoTags.Reforge.FAILURE, failureCount.toStableByte())
        }
    }
}
