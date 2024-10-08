package cc.mewcraft.wakame.item.components.cells.cores

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.item.components.cells.SkillCore
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component

// TODO 移动到专门的渲染系统里

// 文件说明:
// 这里是 CoreSkill 所有跟提示框渲染相关的代码

internal class SkillCoreLoreMetaCreator : DynamicLoreMetaCreator {
    override val namespace: String = Namespaces.SKILL

    override fun test(rawLine: String): Boolean {
        return Key(rawLine).namespace() == namespace
    }

    override fun create(rawTooltipIndex: RawTooltipIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta {
        return SkillCoreLoreMeta(rawTooltipKey = Key(rawLine), rawTooltipIndex = rawTooltipIndex, defaultText = default)
    }
}

internal data class SkillCoreLoreMeta(
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

internal class SkillCoreTooltipKeyProvider(
    private val config: RendererConfig,
) : TooltipKeyProvider<SkillCore> {
    override fun get(obj: SkillCore): TooltipKey? {
        val key = obj.id // 技能的 tooltip key 就是 Core#key
        val rawTooltipKey = key // 技能的 raw tooltip key 与 tooltip key 设计上一致
        if (rawTooltipKey !in config.rawTooltipKeys) {
            return null
        }
        return key
    }
}