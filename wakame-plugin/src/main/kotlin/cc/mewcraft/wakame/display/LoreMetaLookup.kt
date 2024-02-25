package cc.mewcraft.wakame.display

internal interface LoreMetaLookup {
    /**
     * 获取指定的 [key] 在 Item Lore 中的顺序。数值越小，越靠前面。
     *
     * @throws IllegalArgumentException
     * @see LineKeySupplier.get
     */
    fun getIndex(key: FullKey): FullIndex =
        requireNotNull(getIndexOrNull(key)) { "Can't find full index with key '$key'" }

    /**
     * @see getIndex
     */
    fun getIndexOrNull(key: FullKey): FullIndex?

    /**
     * 获取指定的 [key] 在 Item Lore 中的元数据。用此来函数来获得包括顺序在内的更多信息。
     *
     * @throws IllegalArgumentException
     */
    fun <T : LoreMeta> getMeta(key: FullKey): T =
        requireNotNull(getMetaOrNull(key)) { "Can't find lore meta with key '$key'" }

    /**
     * @see getMeta
     */
    fun <T : LoreMeta> getMetaOrNull(key: FullKey): T?
}