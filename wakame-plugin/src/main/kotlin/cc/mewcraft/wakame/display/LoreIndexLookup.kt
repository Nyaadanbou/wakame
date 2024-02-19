package cc.mewcraft.wakame.display

internal interface LoreIndexLookup {
    /**
     * 获取指定的 [key] 在 Item Lore 中的位置。数值越小，越靠前面。
     *
     * @see LineKeySupplier.get
     */
    fun get(key: FullKey): Int
}