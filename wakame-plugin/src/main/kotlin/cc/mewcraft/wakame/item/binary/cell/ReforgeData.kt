package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.item.ShadowTagLike

/**
 * Metadata of reforging.
 *
 * @see ReforgeDataFactory
 */
interface ReforgeData : ShadowTagLike {
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
val ReforgeData.isEmpty: Boolean get() = (this is EmptyReforgeData)

/**
 * Check if the meta is not empty.
 */
val ReforgeData.isNotEmpty: Boolean get() = !isEmpty