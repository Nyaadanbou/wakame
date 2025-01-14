package cc.mewcraft.wakame.display2.implementation.modding_table

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
import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.registry2.KoishRegistries
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable


@ConfigSerializable
internal data class ModdingDifferenceFormats(
    val changeable: ReforgeDifferenceFormat = ReforgeDifferenceFormat(),
    val unchangeable: ReforgeDifferenceFormat = ReforgeDifferenceFormat(),
    val hasInput: ReforgeDifferenceFormat = ReforgeDifferenceFormat(),
    val hasNoInput: ReforgeDifferenceFormat = ReforgeDifferenceFormat(),
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
    override val namespace: String,
    private val ordinal: AttributeCoreOrdinalFormat,
    private val diffFormats: ModdingDifferenceFormats,
) : RendererFormat.Dynamic<AttributeCore> {
    override val textMetaFactory: TextMetaFactory = AttributeCoreTextMetaFactory(namespace, ordinal.operation, ordinal.element)
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace) { id: String -> KoishRegistries.ATTRIBUTE_BUNDLE_FACADE.containsId(id) }

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
internal data class CellularAbilityRendererFormat(
    override val namespace: String,
    private val diffFormats: ModdingDifferenceFormats,
) : RendererFormat.Dynamic<AbilityCore> {
    override val textMetaFactory: TextMetaFactory = AbilityCoreTextMetaFactory(namespace)
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, AbilityRegistry.INSTANCES::has)

    fun render(id: String, core: AbilityCore, context: ModdingTableContext): IndexedText {
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
    private val diffFormats: ModdingDifferenceFormats,
) : RendererFormat.Simple {
    override val id: String = "cells/empty"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = CyclicTextMetaFactory(namespace, id, CyclicIndexRule.SLASH)
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    private val tooltipCycle = IndexedTextCycle(limit = CyclicTextMeta.MAX_DISPLAY_COUNT) { i ->
        SimpleIndexedText(CyclicIndexRule.SLASH.make(index, i), tooltip)
    }

    fun render(id: String, context: ModdingTableContext): IndexedText {
        val next = tooltipCycle.next()
        val original = next.text
        val processed = diffFormats.render(id, original, context)
        return next.copy(text = processed)
    }
}
