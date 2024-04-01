package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.item.ShadowTagLike

/**
 * Metadata of reforging.
 *
 * @see ReforgeMetaFactory
 * @see NekoTags.Reforge
 */
interface ReforgeMeta : ShadowTagLike {
    /**
     * 重铸成功的次数。
     */
    val successCount: Int

    /**
     * 重铸失败的次数。
     */
    val failureCount: Int
}

/**
 * Check if the meta is empty.
 */
val ReforgeMeta.isEmpty: Boolean get() = (this is EmptyReforgeMeta)

/**
 * Check if the meta is not empty.
 */
val ReforgeMeta.isNotEmpty: Boolean get() = !isEmpty