package cc.mewcraft.wakame.item.components.legacy

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.display.DynamicLoreMeta
import cc.mewcraft.wakame.display.DynamicLoreMetaCreator
import cc.mewcraft.wakame.display.DynamicLoreMetaCreatorRegistry
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.RawTooltipIndex
import cc.mewcraft.wakame.display.RawTooltipKey
import cc.mewcraft.wakame.display.RendererConfig
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PostWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// 文件说明:
// 这里是 ItemMeta 的所有跟提示框渲染相关的代码

@PostWorldDependency(
    runAfter = [RendererConfig::class]
)
@ReloadDependency(
    runAfter = [RendererConfig::class]
)
internal object ItemMetaBootstrap : Initializable, KoinComponent {
    private val dynamicLoreMetaCreatorRegistry by inject<DynamicLoreMetaCreatorRegistry>()

    override fun onPostWorld() {
        dynamicLoreMetaCreatorRegistry.register(ItemMetaLoreMetaCreator())
    }
}

internal class ItemMetaLoreMetaCreator : DynamicLoreMetaCreator {
    override val namespace: String = Namespaces.ITEM_META

    override fun test(rawLine: String): Boolean {
        return Key(rawLine).namespace() == namespace
    }

    override fun create(rawTooltipIndex: RawTooltipIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta {
        return ItemMetaLoreMeta(rawTooltipKey = Key(rawLine), rawTooltipIndex = rawTooltipIndex, defaultText = default)
    }
}

internal data class ItemMetaLoreMeta(
    override val rawTooltipKey: RawTooltipKey,
    override val rawTooltipIndex: RawTooltipIndex,
    override val defaultText: List<Component>?,
) : DynamicLoreMeta {
    override fun generateTooltipKeys(): List<TooltipKey> {
        return listOf(rawTooltipKey)
    }

    override fun createDefault(): List<LoreLine>? {
        if (defaultText.isNullOrEmpty()) {
            return null
        }
        return generateTooltipKeys().map { key -> LoreLine.simple(key, defaultText) }
    }
}