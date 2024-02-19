package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.util.getOrThrow

internal class LoreIndexLookupImpl(
    private val indexes: Map<FullKey, Int>
) : LoreIndexLookup {
    override fun get(key: FullKey): Int {
        return indexes.getOrThrow(key)
    }
}