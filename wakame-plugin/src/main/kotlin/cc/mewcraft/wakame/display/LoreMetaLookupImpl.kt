package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.util.getOrThrow

internal class LoreMetaLookupImpl(
    private val indexes: Map<FullKey, FullIndex>,
    private val metadata: Map<FullKey, LoreMeta>,
) : LoreMetaLookup {
    override fun getIndex(key: FullKey): FullIndex = indexes.getOrThrow(key)
    override fun getMeta(key: FullKey): LoreMeta = metadata.getOrThrow(key)
}