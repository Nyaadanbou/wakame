package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.util.compoundShadowTag
import cc.mewcraft.wakame.util.toStableByte
import me.lucko.helper.shadows.nbt.ShadowTag

internal data class ReforgeMetaImpl(
    override val successCount: Int,
    override val failureCount: Int,
) : ReforgeMeta {

    override fun asShadowTag(): ShadowTag {
        return compoundShadowTag {
            putByte(NekoTags.Reforge.SUCCESS, successCount.toStableByte())
            putByte(NekoTags.Reforge.FAILURE, failureCount.toStableByte())
        }
    }

}
