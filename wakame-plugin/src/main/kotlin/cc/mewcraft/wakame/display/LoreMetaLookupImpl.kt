package cc.mewcraft.wakame.display

internal class LoreMetaLookupImpl(
    private val indexes: Map<TooltipKey, TooltipIndex>,
    private val metadata: Map<TooltipKey, LoreMeta>,
) : LoreMetaLookup {
    override fun getIndexOrNull(key: TooltipKey): TooltipIndex? {
        return indexes[key]
    }

    override fun <T : LoreMeta> getMetaOrNull(key: TooltipKey): T? {
        @Suppress("UNCHECKED_CAST")
        return (metadata[key] as? T)
    }
}