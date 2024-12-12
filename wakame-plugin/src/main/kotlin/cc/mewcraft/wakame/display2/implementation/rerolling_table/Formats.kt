package cc.mewcraft.wakame.display2.implementation.rerolling_table

import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.TextMetaFactory
import cc.mewcraft.wakame.display2.TextMetaFactoryPredicate
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
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.item.components.cells.SkillCore
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.SkillRegistry
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting


// 开发日记 2024/10/25
// 对于如何渲染要重造的核孔这个问题, 直接把*原输入*在输出容器里显示,
// 但把要重造的核孔划上删除线并加上类似“???”的前缀/后缀,
// 这样应该就足矣表示这个核孔将要经历重造了.

@ConfigSerializable
internal data class RerollingDifferenceFormats(
    @Setting
    val changeable: DifferenceFormat = DifferenceFormat(),
    @Setting
    val unchangeable: DifferenceFormat = DifferenceFormat(),
    @Setting
    val selected: DifferenceFormat = DifferenceFormat(),
    @Setting
    val unselected: DifferenceFormat = DifferenceFormat(),
) {
    /**
     * @param id 核孔的 id
     * @param source 原核心的描述
     * @param context 重造台的上下文
     * @return 基于 [id], [core], [context] 生成的 [IndexedText]
     */
    fun render(id: String, source: List<Component>, context: RerollingTableContext): List<Component> {
        val selectionMap = context.session.selectionMap
        val selection = selectionMap[id]

        var result = source

        if (selection.changeable) {
            result = changeable.process(result)

            if (selection.selected) {
                result = selected.process(result)
            } else {
                result = unselected.process(result)
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
    @Setting("diff_formats")
    @Required
    private val differenceFormats: RerollingDifferenceFormats,
) : RendererFormat.Dynamic<AttributeCore> {
    override val textMetaFactory: TextMetaFactory = AttributeCoreTextMetaFactory(namespace, ordinal.operation, ordinal.element)
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, AttributeRegistry.FACADES::has)

    /**
     * @param id 核孔的 id
     * @param core 属性核心
     * @param context 重造台的上下文
     */
    fun render(id: String, core: AttributeCore, context: RerollingTableContext): IndexedText {
        val original = core.description
        val processed = differenceFormats.render(id, original, context)
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
    @Setting("diff_formats")
    @Required
    private val differenceFormats: RerollingDifferenceFormats,
) : RendererFormat.Dynamic<SkillCore> {
    override val textMetaFactory: TextMetaFactory = SkillCoreTextMetaFactory(namespace)
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, SkillRegistry.INSTANCES::has)

    /**
     * @param id 核孔的 id
     * @param core 技能核心
     * @param context 重造台的上下文
     */
    fun render(id: String, core: SkillCore, context: RerollingTableContext): IndexedText {
        val original = core.description
        val processed = differenceFormats.render(id, original, context)
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
    private val tooltip: List<Component> = listOf(text("Empty Slot")),
    @Setting("diff_formats")
    @Required
    private val differenceFormats: RerollingDifferenceFormats,
) : RendererFormat.Simple {
    override val id: String = "cells/empty"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = CyclicTextMetaFactory(namespace, id, CyclicIndexRule.SLASH)
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    private val tooltipCycle = IndexedTextCycle(limit = CyclicTextMeta.MAX_DISPLAY_COUNT) { i ->
        SimpleIndexedText(CyclicIndexRule.SLASH.make(index, i), tooltip)
    }

    /**
     * @param id 核孔的 id
     * @param core 空核心
     * @param context 重造台的上下文
     */
    fun render(id: String, core: EmptyCore, context: RerollingTableContext): IndexedText {
        val next = tooltipCycle.next()
        val original = next.text
        val processed = differenceFormats.render(id, original, context)
        return next.copy(text = processed)
    }
}