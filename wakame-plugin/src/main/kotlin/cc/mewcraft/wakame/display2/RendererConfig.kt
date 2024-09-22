package cc.mewcraft.wakame.display2

import net.kyori.adventure.key.Key

interface RendererConfig {
    val textOrdinalMap: Map<Key, Int>
    val textMetadataMap: Map<Key, TextMeta>

    val staticIndexedTextList: List<IndexedText>
    val defaultIndexedTextList: List<IndexedText>

    fun load()
}