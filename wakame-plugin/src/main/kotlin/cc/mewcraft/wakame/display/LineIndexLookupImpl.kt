package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.util.getOrThrow

internal class LineIndexLookupImpl(
    private val indexes: Map<FullKey, Int>
) : LineIndexLookup {
    override fun get(key: FullKey): Int {
        return indexes.getOrThrow(key)
    }
}