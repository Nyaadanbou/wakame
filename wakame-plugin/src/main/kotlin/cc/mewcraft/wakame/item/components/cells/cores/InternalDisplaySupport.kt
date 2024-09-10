package cc.mewcraft.wakame.item.components.cells.cores

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.display2.RendererSystems
import cc.mewcraft.wakame.initializer.*
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentRegistry
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// TODO 移动到专门的渲染系统里

// 文件说明:
// 这里是 CoreEmpty 的所有跟提示框渲染相关的代码

internal object EmptyCoreDisplaySupport {
    /**
     * 最多能有几个空核心被渲染出来.
     */
    val MAX_DISPLAY_COUNT by ItemComponentRegistry.CONFIG.derive(ItemConstants.CELLS).entry<Int>("max_visible_empty_cores")

    /**
     * 根据索引生成对应的 [TooltipKey].
     */
    fun derive(rawTooltipKey: RawTooltipKey, index: Int): TooltipKey {
        return Key("${rawTooltipKey.namespace()}:${rawTooltipKey.value()}/$index")
    }
}

@ReloadDependency(
    runAfter = [RendererBootstrap::class]
)
@PostWorldDependency(
    runAfter = [RendererBootstrap::class]
)
internal object EmptyCoreBootstrap : Initializable, KoinComponent {
    private val dynamicLoreMetaCreators by inject<DynamicLoreMetaCreators>()

    override fun onPostWorld() {
        for ((systemName, _) in RendererSystems.entries()) {
            dynamicLoreMetaCreators.register(systemName, EmptyCoreLoreMetaCreator())
        }
    }
}

internal class EmptyCoreLoreMetaCreator : DynamicLoreMetaCreator {
    override val namespace: String = GenericKeys.EMPTY.namespace()

    override fun test(rawLine: String): Boolean {
        return Key(rawLine) == GenericKeys.EMPTY
    }

    override fun create(rawTooltipIndex: RawTooltipIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta {
        return EmptyCoreLoreMeta(
            rawTooltipKey = Key(rawLine),
            rawTooltipIndex = rawTooltipIndex,
            defaultText = default
        )
    }
}

internal data class EmptyCoreLoreMeta(
    override val rawTooltipKey: RawTooltipKey,
    override val rawTooltipIndex: RawTooltipIndex,
    override val defaultText: List<Component>?,
) : DynamicLoreMeta {
    // 根据 MAX_DISPLAY_COUNT 生成对应数量的 TooltipKey. 生成出来的格式为:
    // "namespace:value/0",
    // "namespace:value/1",
    // "namespace:value/2", ...
    override fun generateTooltipKeys(): List<TooltipKey> {
        val ret = mutableListOf<TooltipKey>()
        for (i in 0 until EmptyCoreDisplaySupport.MAX_DISPLAY_COUNT) {
            ret += EmptyCoreDisplaySupport.derive(rawTooltipKey, i)
        }
        return ret
    }

    override fun createDefault(): List<LoreLine>? {
        if (defaultText.isNullOrEmpty()) {
            return null
        }
        return generateTooltipKeys().map { key -> LoreLine.simple(key, defaultText) }
    }
}
