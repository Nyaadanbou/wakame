package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.util.getOrThrow

internal class LoreMetaLookupImpl(
    private val indexes: Map<FullKey, FullIndex>,
    private val metadata: Map<FullKey, LoreMeta>,
) : LoreMetaLookup {
    override fun getIndex(key: FullKey): FullIndex = indexes.getOrThrow(key)
    override fun <T : LoreMeta> getMeta(key: FullKey): T = @Suppress("UNCHECKED_CAST") (metadata.getOrThrow(key) as T)
}