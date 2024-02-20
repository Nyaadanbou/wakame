package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.util.getOrThrow

internal class LoreIndexLookupImpl(
    private val indexes: Map<FullKey, FullIndex>,
) : LoreIndexLookup {
    override fun get(key: FullKey): FullIndex {
        return indexes.getOrThrow(key)
    }
}