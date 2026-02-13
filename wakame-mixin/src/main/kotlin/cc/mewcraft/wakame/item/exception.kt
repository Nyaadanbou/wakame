package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.util.KoishKey

/**
 * 代表在生成一个 ItemStack 时发生了错误.
 *
 * @param item 物品的唯一标识
 */
class ItemStackGenerationException(
    val item: KoishKey,
    cause: Throwable? = null,
) : RuntimeException(
    "Failed to generate item stack for item $item", cause
) {

    override fun toString(): String {
        return "ItemStackGenerationException(item=${item}, cause=${cause?.message})"
    }

}