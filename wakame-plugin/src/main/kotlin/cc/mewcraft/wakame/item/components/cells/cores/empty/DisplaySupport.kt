package cc.mewcraft.wakame.item.components.cells.cores.empty

import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.config.entry
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
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.registry.ItemComponentRegistry
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// 文件说明:
// 这里是 CoreEmpty 的所有跟提示框渲染相关的代码

@ReloadDependency(
    runAfter = [RendererConfig::class]
)
@PostWorldDependency(
    runAfter = [RendererConfig::class]
)
internal object CoreEmptyBootstrap : Initializable, KoinComponent {
    private val dynamicLoreMetaCreatorRegistry by inject<DynamicLoreMetaCreatorRegistry>()

    override fun onPostWorld() {
        dynamicLoreMetaCreatorRegistry.register(CoreEmptyLoreMetaCreator())
    }
}

internal class CoreEmptyLoreMetaCreator : DynamicLoreMetaCreator {
    override val namespace: String = GenericKeys.EMPTY.namespace()

    override fun test(rawLine: String): Boolean {
        return Key(rawLine) == GenericKeys.EMPTY
    }

    override fun create(rawTooltipIndex: RawTooltipIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta {
        return CoreEmptyLoreMeta(rawTooltipKey = Key(rawLine), rawTooltipIndex = rawTooltipIndex, defaultText = default)
    }
}

internal data object CoreEmptyLoreLine : LoreLine {
    override val key: TooltipKey = GenericKeys.EMPTY
    override val content: List<Component> by ItemComponentRegistry.CONFIG
        .derive(ItemComponentConstants.CELLS)
        .entry<Component>("tooltips", "empty")
        .map(::listOf)
}

internal data class CoreEmptyLoreMeta(
    override val rawTooltipKey: RawTooltipKey,
    override val rawTooltipIndex: RawTooltipIndex,
    override val defaultText: List<Component>?,
) : DynamicLoreMeta {
    override fun generateTooltipKeys(): List<TooltipKey> = listOf(rawTooltipKey)
    override fun createDefault(): List<LoreLine>? = null
}
