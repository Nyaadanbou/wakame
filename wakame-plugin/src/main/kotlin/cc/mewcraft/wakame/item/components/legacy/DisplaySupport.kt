package cc.mewcraft.wakame.item.components.legacy

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.display2.RendererSystems
import cc.mewcraft.wakame.initializer.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// TODO 迁移到新的渲染系统
// FIXME namespace 支持自定义

// 文件说明:
// 这里是 ItemMeta 的所有跟提示框渲染相关的代码

@PostWorldDependency(
    runAfter = [RendererBootstrap::class]
)
@ReloadDependency(
    runAfter = [RendererBootstrap::class]
)
internal object ItemMetaBootstrap : Initializable, KoinComponent {
    private val dynamicLoreMetaCreators by inject<DynamicLoreMetaCreators>()

    override fun onPostWorld() {
        for ((systemName, _) in RendererSystems.entries()) {
            dynamicLoreMetaCreators.register(systemName, ItemMetaLoreMetaCreator())
        }
    }
}

internal class ItemMetaLoreMetaCreator : DynamicLoreMetaCreator {
    override val namespace: String = Namespaces.ITEM_META

    override fun test(rawLine: String): Boolean {
        return Key.key(rawLine).namespace() == namespace
    }

    override fun create(rawTooltipIndex: RawTooltipIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta {
        return ItemMetaLoreMeta(rawTooltipKey = Key.key(rawLine), rawTooltipIndex = rawTooltipIndex, defaultText = default)
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