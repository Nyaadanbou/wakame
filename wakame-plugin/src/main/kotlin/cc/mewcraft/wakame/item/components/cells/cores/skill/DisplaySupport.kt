package cc.mewcraft.wakame.item.components.cells.cores.skill

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.display.DynamicLoreMeta
import cc.mewcraft.wakame.display.DynamicLoreMetaCreator
import cc.mewcraft.wakame.display.DynamicLoreMetaCreators
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.RawTooltipIndex
import cc.mewcraft.wakame.display.RawTooltipKey
import cc.mewcraft.wakame.display.RendererBootstrap
import cc.mewcraft.wakame.display.RendererConfig
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipKeyProvider
import cc.mewcraft.wakame.display2.RendererSystems
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PostWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// 文件说明:
// 这里是 CoreSkill 所有跟提示框渲染相关的代码

@ReloadDependency(
    runAfter = [RendererBootstrap::class]
)
@PostWorldDependency(
    runAfter = [RendererBootstrap::class]
)
internal object CoreSkillBootstrap : Initializable, KoinComponent {
    private val dynamicLoreMetaCreators by inject<DynamicLoreMetaCreators>()

    override fun onPostWorld() {
        for ((systemName, _) in RendererSystems.entries()) {
            dynamicLoreMetaCreators.register(systemName, CoreSkillLoreMetaCreator())
        }
    }
}

internal class CoreSkillLoreMetaCreator : DynamicLoreMetaCreator {
    override val namespace: String = Namespaces.SKILL

    override fun test(rawLine: String): Boolean {
        return Key(rawLine).namespace() == namespace
    }

    override fun create(rawTooltipIndex: RawTooltipIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta {
        return CoreSkillLoreMeta(rawTooltipKey = Key(rawLine), rawTooltipIndex = rawTooltipIndex, defaultText = default)
    }
}

internal data class CoreSkillLoreMeta(
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

internal class CoreSkillTooltipKeyProvider(
    private val config: RendererConfig,
) : TooltipKeyProvider<CoreSkill> {
    override fun get(obj: CoreSkill): TooltipKey? {
        val key = obj.key // 技能的 tooltip key 就是 Core#key
        val rawTooltipKey = key // 技能的 raw tooltip key 与 tooltip key 设计上一致
        if (rawTooltipKey !in config.rawTooltipKeys) {
            return null
        }
        return key
    }
}