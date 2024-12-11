package cc.mewcraft.wakame.display2.implementation.modding_table

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.implementation.common.AttributeCoreOrdinalFormat
import cc.mewcraft.wakame.display2.implementation.common.CyclicIndexRule
import cc.mewcraft.wakame.display2.implementation.common.CyclicTextMeta
import cc.mewcraft.wakame.display2.implementation.common.CyclicTextMetaFactory
import cc.mewcraft.wakame.display2.implementation.common.DifferenceFormat
import cc.mewcraft.wakame.display2.implementation.common.IndexedTextCycle
import cc.mewcraft.wakame.display2.implementation.common.computeIndex
import cc.mewcraft.wakame.display2.implementation.standard.AttributeCoreTextMeta
import cc.mewcraft.wakame.display2.implementation.standard.AttributeCoreTextMetaFactory
import cc.mewcraft.wakame.display2.implementation.standard.SkillCoreTextMetaFactory
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.SkillCore
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting


@ConfigSerializable
internal data class ModdingDifferenceFormats(
    @Setting
    val changeable: DifferenceFormat = DifferenceFormat(),
    @Setting
    val unchangeable: DifferenceFormat = DifferenceFormat(),
    @Setting
    val hasInput: DifferenceFormat = DifferenceFormat(),
    @Setting
    val hasNoInput: DifferenceFormat = DifferenceFormat(),
) {
    /**
     * @param id 核孔的 id
     * @param source 原核心的描述
     * @param context 重造台的上下文
     * @return 基于 [id], [core], [context] 生成的 [IndexedText]
     */
    fun render(id: String, source: List<Component>, context: ModdingTableContext): List<Component> {
        val replaceMap = context.session.replaceParams
        val replace = replaceMap[id]

        var result = source

        if (replace.changeable) {
            result = changeable.process(result)

            if (replace.originalInput != null) {
                result = hasInput.process(result)
            } else {
                result = hasNoInput.process(result)
            }

        } else {
            result = unchangeable.process(result)
        }

        return result
    }
}

@ConfigSerializable
internal data class CellularAttributeRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @Required
    private val ordinal: AttributeCoreOrdinalFormat,
    @Setting @Required
    private val diffFormats: ModdingDifferenceFormats,
) : RendererFormat.Dynamic<AttributeCore> {
    override val textMetaFactory = AttributeCoreTextMetaFactory(namespace, ordinal.operation, ordinal.element)

    /**
     * @param id 核孔的 id
     */
    fun render(id: String, core: AttributeCore, context: ModdingTableContext): IndexedText {
        val original = core.description
        val processed = diffFormats.render(id, original, context)
        return SimpleIndexedText(computeIndex(core), processed)
    }

    /**
     * 实现要求: 返回值必须是 [AttributeCoreTextMeta.derivedIndexes] 的子集.
     */
    override fun computeIndex(data: AttributeCore): Key {
        return data.computeIndex(namespace)
    }
}

@ConfigSerializable
internal data class CellularSkillRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @Required
    private val diffFormats: ModdingDifferenceFormats,
) : RendererFormat.Dynamic<SkillCore> {
    override val textMetaFactory = SkillCoreTextMetaFactory(namespace)

    fun render(id: String, core: SkillCore, context: ModdingTableContext): IndexedText {
        val original = core.description
        val processed = diffFormats.render(id, original, context)
        return SimpleIndexedText(computeIndex(core), processed)
    }

    override fun computeIndex(data: SkillCore): Key {
        val skill = data.skill
        val dataId = skill.id
        val indexId = dataId.namespace() + "/" + dataId.value()
        return Key.key(namespace, indexId)
    }
}

@ConfigSerializable
internal data class CellularEmptyRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting
    private val tooltip: List<Component> = listOf(Component.text("Empty Slot")),
    @Setting @Required
    private val diffFormats: ModdingDifferenceFormats,
) : RendererFormat.Simple {
    override val id = "cells/empty"
    override val index = createIndex()

    private val cyclicIndexRule = CyclicIndexRule.SLASH
    override val textMetaFactory = CyclicTextMetaFactory(namespace, id, cyclicIndexRule)

    private val tooltipCycle = IndexedTextCycle(limit = CyclicTextMeta.MAX_DISPLAY_COUNT) { i ->
        SimpleIndexedText(cyclicIndexRule.make(index, i), tooltip)
    }

    fun render(id: String, context: ModdingTableContext): IndexedText {
        val next = tooltipCycle.next()
        val original = next.text
        val processed = diffFormats.render(id, original, context)
        return next.copy(text = processed)
    }
}
