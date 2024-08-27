package cc.mewcraft.wakame.display

internal interface LoreMetaLookup {
    companion object {
        fun create(
            indexes: Map<TooltipKey, TooltipIndex>,
            metadata: Map<TooltipKey, LoreMeta>,
        ): LoreMetaLookup {
            return LoreMetaLookupImpl(indexes, metadata)
        }
    }

    /**
     * 获取指定的 [key] 在 Item Lore 中的顺序. 数值越小, 越靠前面.
     *
     * @throws IllegalArgumentException
     * @see TooltipKeyProvider.get
     */
    fun getIndex(key: TooltipKey): TooltipIndex =
        requireNotNull(getIndexOrNull(key)) { "Can't find TooltipIndex with key '$key'" }

    /**
     * @see getIndex
     */
    fun getIndexOrNull(key: TooltipKey): TooltipIndex?

    /**
     * 获取指定的 [key] 在 Item Lore 中的元数据. 用此来函数来获得包括顺序在内的更多信息.
     *
     * @throws IllegalArgumentException
     */
    fun <T : LoreMeta> getMeta(key: TooltipKey): T =
        requireNotNull(getMetaOrNull(key)) { "Can't find lore meta with key '$key'" }

    /**
     * @see getMeta
     */
    fun <T : LoreMeta> getMetaOrNull(key: TooltipKey): T?
}

private class LoreMetaLookupImpl(
    private val indexes: Map<TooltipKey, TooltipIndex>,
    private val metadata: Map<TooltipKey, LoreMeta>,
) : LoreMetaLookup {
    override fun getIndexOrNull(key: TooltipKey): TooltipIndex? {
        return indexes[key]
    }

    override fun <T : LoreMeta> getMetaOrNull(key: TooltipKey): T? {
        @Suppress("UNCHECKED_CAST")
        return metadata[key] as? T
    }
}