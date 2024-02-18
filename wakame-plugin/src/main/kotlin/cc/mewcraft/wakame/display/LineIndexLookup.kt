package cc.mewcraft.wakame.display

internal interface LineIndexLookup {
    /**
     * 获取指定 [key] 在 lore 中的位置。数值越小，越靠前面。
     *
     * @see LineKeySupplier.get
     */
    fun get(key: FullKey): Int
}