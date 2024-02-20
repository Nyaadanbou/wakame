package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.util.getOrThrow

internal class LoreMetaLookupImpl(
    private val indexes: Map<FullKey, FullIndex>,
) : LoreMetaLookup {
    override fun get(key: FullKey): FullIndex {
        return indexes.getOrThrow(key)
    }
}