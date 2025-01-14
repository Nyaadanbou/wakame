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
import cc.mewcraft.wakame.display2.implementation.common.IndexedTextCycle
import cc.mewcraft.wakame.display2.implementation.common.ReforgeDifferenceFormat
import cc.mewcraft.wakame.display2.implementation.common.computeIndex
import cc.mewcraft.wakame.display2.implementation.standard.AbilityCoreTextMetaFactory
import cc.mewcraft.wakame.display2.implementation.standard.AttributeCoreTextMeta
import cc.mewcraft.wakame.display2.implementation.standard.AttributeCoreTextMetaFactory
import cc.mewcraft.wakame.item.components.cells.AbilityCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.registry2.KoishRegistries
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable


// 开发日记 2024/10/25
// 对于如何渲染要重造的核孔这个问题, 直接把*原输入*在输出容器里显示,
// 但把要重造的核孔划上删除线并加上类似“???”的前缀/后缀,
// 这样应该就足矣表示这个核孔将要经历重造了.

@ConfigSerializable
internal data class RerollingDifferenceFormats(
    val changeable: ReforgeDifferenceFormat = ReforgeDifferenceFormat(),
    val unchangeable: ReforgeDifferenceFormat = ReforgeDifferenceFormat(),
    val selected: ReforgeDifferenceFormat = ReforgeDifferenceFormat(),
    val unselected: ReforgeDifferenceFormat = ReforgeDifferenceFormat(),
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
    override val namespace: String,
    private val ordinal: AttributeCoreOrdinalFormat,
    private val diffFormats: RerollingDifferenceFormats,
) : RendererFormat.Dynamic<AttributeCore> {
    override val textMetaFactory: TextMetaFactory = AttributeCoreTextMetaFactory(namespace, ordinal.operation, ordinal.element)
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace) { id: String -> KoishRegistries.ATTRIBUTE_BUNDLE_FACADE.containsId(id) }

    /**
     * @param id 核孔的 id
     * @param core 属性核心
     * @param context 重造台的上下文
     */
    fun render(id: String, core: AttributeCore, context: RerollingTableContext): IndexedText {
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
internal data class CellularAbilityRendererFormat(
    override val namespace: String,
    private val diffFormats: RerollingDifferenceFormats,
) : RendererFormat.Dynamic<AbilityCore> {
    override val textMetaFactory: TextMetaFactory = AbilityCoreTextMetaFactory(namespace)
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, AbilityRegistry.INSTANCES::has)

    /**
     * @param id 核孔的 id
     * @param core 技能核心
     * @param context 重造台的上下文
     */
    fun render(id: String, core: AbilityCore, context: RerollingTableContext): IndexedText {
        val original = core.description
        val processed = diffFormats.render(id, original, context)
        return SimpleIndexedText(computeIndex(core), processed)
    }

    override fun computeIndex(data: AbilityCore): Key {
        val ability = data.ability
        val dataId = ability.id
        val indexId = dataId.namespace() + "/" + dataId.value()
        return Key.key(namespace, indexId)
    }
}

@ConfigSerializable
internal data class CellularEmptyRendererFormat(
    override val namespace: String,
    private val tooltip: List<Component>,
    private val diffFormats: RerollingDifferenceFormats,
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
        val processed = diffFormats.render(id, original, context)
        return next.copy(text = processed)
    }
}