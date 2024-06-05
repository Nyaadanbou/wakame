package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.display.DisplaySupport
import cc.mewcraft.wakame.display.DynamicLoreMeta
import cc.mewcraft.wakame.display.DynamicLoreMetaCreator
import cc.mewcraft.wakame.display.FullKey
import cc.mewcraft.wakame.display.LineKeyFactory
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.RawIndex
import cc.mewcraft.wakame.display.RawKey
import cc.mewcraft.wakame.display.RendererConfiguration
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PostWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component

@PostWorldDependency(runAfter = [RendererConfiguration::class])
@ReloadDependency(runAfter = [RendererConfiguration::class])
internal object ItemMetaInitializer : Initializable {
    override fun onPostWorld() {
        DisplaySupport.DYNAMIC_LORE_META_CREATOR_REGISTRY.register(ItemMetaLoreMetaCreator())
        DisplaySupport.LOGGER.info("Registered DynamicLoreMetaCreator for item meta")
    }
}

internal class ItemMetaLoreMetaCreator : DynamicLoreMetaCreator {
    override val namespace: String = Namespaces.ITEM_META

    override fun test(rawLine: String): Boolean {
        return Key(rawLine).namespace() == namespace
    }

    override fun create(rawIndex: RawIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta {
        return ItemMetaLoreMeta(rawKey = Key(rawLine), rawIndex = rawIndex, default = default)
    }
}

internal data class ItemMetaLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
) : DynamicLoreMeta {
    override fun generateFullKeys(): List<FullKey> {
        return listOf(rawKey)
    }

    override fun createDefault(): List<LoreLine>? {
        if (default.isNullOrEmpty()) {
            return null
        }
        return generateFullKeys().map { key -> LoreLine.simple(key, default) }
    }
}

internal class ItemMetaLineKeyFactory(
    private val config: RendererConfiguration,
) : LineKeyFactory<BinaryItemMeta<*>> {
    override fun get(obj: BinaryItemMeta<*>): FullKey? {
        val fullKey = obj.key // 元数据的 full key 就是 BinaryItemMeta#key
        val rawKey = fullKey // 元数据的 raw key 跟它的 full key 设计上一致
        if (rawKey !in config.rawKeys) {
            return null
        }
        return fullKey
    }
}
