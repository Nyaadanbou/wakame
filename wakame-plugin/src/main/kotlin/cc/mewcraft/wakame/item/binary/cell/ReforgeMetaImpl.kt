package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.util.compoundShadowTag
import cc.mewcraft.wakame.util.toStableByte
import me.lucko.helper.shadows.nbt.ShadowTag

internal class ReforgeMetaImpl(
    override val successCount: Int,
    override val failureCount: Int,
) : ReforgeMeta {
    override fun asShadowTag(): ShadowTag {
        return compoundShadowTag {
            putByte(ReforgeMetaTagNames.SUCCESS, successCount.toStableByte())
            putByte(ReforgeMetaTagNames.FAILURE, failureCount.toStableByte())
        }
    }
}
