package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component

internal data class ItemMetaLoreLine(
    override val key: FullKey,
    override val lines: List<Component>,
) : LoreLine

internal data class ItemMetaLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
) : DynamicLoreMeta {
    override val fullKeys: List<FullKey> = listOf(Key(rawKey.namespace(), rawKey.value()))
}

internal class ItemMetaLineKeyFactory(
    private val config: RendererConfiguration,
) : LineKeyFactory<BinaryItemMeta<*>> {
    override fun get(obj: BinaryItemMeta<*>): FullKey {
        val fullKey = obj.key // 元数据的 full key 就是 BinaryItemMeta#key
        val rawKey = fullKey // 元数据的 raw key 跟它的 full key 设计上一致
        return if (rawKey !in config.rawKeys) {
            LineKeyFactory.SKIP_DISPLAY
        } else {
            fullKey
        }
    }
}
