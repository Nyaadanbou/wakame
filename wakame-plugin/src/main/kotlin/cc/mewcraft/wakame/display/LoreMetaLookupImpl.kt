package cc.mewcraft.wakame.display

internal class LoreMetaLookupImpl(
    private val indexes: Map<FullKey, FullIndex>,
    private val metadata: Map<FullKey, LoreMeta>,
) : LoreMetaLookup {
    override fun getIndexOrNull(key: FullKey): FullIndex? =
        indexes[key]

    override fun <T : LoreMeta> getMetaOrNull(key: FullKey): T? =
        @Suppress("UNCHECKED_CAST") (metadata[key] as? T)
}