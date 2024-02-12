package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.annotation.InternalApi
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag

/**
 * 代表一个空的重铸元数据。
 */
@InternalApi
internal object EmptyReforgeMeta : ReforgeMeta {
    override val successCount: Int get() = throw UnsupportedOperationException("EmptyReforgeMeta has no success count")
    override val failureCount: Int get() = throw UnsupportedOperationException("EmptyReforgeMeta has no success count")

    override fun asShadowTag(): ShadowTag = CompoundShadowTag.create()
}