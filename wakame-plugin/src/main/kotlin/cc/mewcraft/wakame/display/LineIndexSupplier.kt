package cc.mewcraft.wakame.display

import net.kyori.adventure.key.Key

internal interface LineIndexSupplier {
    /**
     * 获取指定 [key] 在 lore 中的位置。数值越小，越靠前面。
     *
     * @see LineKeySupplier.getKey
     */
    fun getIndex(key: Key): Int
}