package cc.mewcraft.wakame.item.components.legacy

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.display.DisplaySupport
import cc.mewcraft.wakame.display.DynamicLoreMeta
import cc.mewcraft.wakame.display.DynamicLoreMetaCreator
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.RawIndex
import cc.mewcraft.wakame.display.RawKey
import cc.mewcraft.wakame.display.RendererConfig
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PostWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component

// TODO 2024/7/6 重构 ItemMeta (legacy) 更好的与组件系统结合

@PostWorldDependency(runAfter = [RendererConfig::class])
@ReloadDependency(runAfter = [RendererConfig::class])
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
    override fun generateFullKeys(): List<TooltipKey> {
        return listOf(rawKey)
    }

    override fun createDefault(): List<LoreLine>? {
        if (default.isNullOrEmpty()) {
            return null
        }
        return generateFullKeys().map { key -> LoreLine.simple(key, default) }
    }
}
