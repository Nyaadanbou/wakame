package cc.mewcraft.wakame.display

internal class LoreMetaLookupImpl(
    private val indexes: Map<TooltipKey, TooltipIndex>,
    private val metadata: Map<TooltipKey, LoreMeta>,
) : LoreMetaLookup {
    constructor(rendererConfig: RendererConfig) : this(
        rendererConfig.loreIndexLookup,
        rendererConfig.loreMetaLookup
    )

    override fun getIndexOrNull(key: TooltipKey): TooltipIndex? {
        return indexes[key]
    }

    override fun <T : LoreMeta> getMetaOrNull(key: TooltipKey): T? {
        @Suppress("UNCHECKED_CAST")
        return metadata[key] as? T
    }
}