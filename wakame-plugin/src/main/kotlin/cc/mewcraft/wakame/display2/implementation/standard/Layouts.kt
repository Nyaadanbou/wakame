package cc.mewcraft.wakame.display2.implementation.standard

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.composite.CompositeAttributeComponent
import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.display2.SimpleTextMeta
import cc.mewcraft.wakame.display2.SourceIndex
import cc.mewcraft.wakame.display2.SourceOrdinal
import cc.mewcraft.wakame.display2.TextMetaFactory
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.registry.hasComponent
import cc.mewcraft.wakame.util.StringCombiner
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component


internal data class AttributeCoreTextMeta(
    override val sourceIndex: SourceIndex,
    override val sourceOrdinal: SourceOrdinal,
    override val defaultText: List<Component>?,
    private val derivation: DerivationRule,
) : SimpleTextMeta {
    override val derivedIndexes: List<DerivedIndex> = deriveIndexes()

    /**
     * 实现要求: 返回的列表必须是 [CellularAttributeRendererFormat.computeIndex] 的超集.
     */
    override fun deriveIndexes(): List<DerivedIndex> {
        val sourceNamespace = sourceIndex.namespace()
        val sourceId = sourceIndex.value()
        val combiner = StringCombiner(sourceId, ".") {
            addList(derivation.operationIndex)
            addList(derivation.elementIndex, AttributeRegistry.FACADES[sourceId].components.hasComponent<CompositeAttributeComponent.Element>())
        }
        val combinations = combiner.combine()
        return combinations.map { Key.key(sourceNamespace, it) }
    }

    data class DerivationRule(
        val operationIndex: List<String>,
        val elementIndex: List<String>,
    ) {
        init { // validate values
            this.operationIndex.forEach { Operation.byName(it) ?: error("'$it' is not a valid operation, check your renderer config") }
            this.elementIndex.forEach { ElementRegistry.INSTANCES.getOrNull(it) ?: error("'$it' is not a valid element, check your renderer config") }
        }
    }
}

internal data class AttributeCoreTextMetaFactory(
    override val namespace: String,
    private val operationIndex: List<String>,
    private val elementIndex: List<String>,
) : TextMetaFactory {
    override fun test(sourceIndex: Key): Boolean {
        return sourceIndex.namespace() == namespace && AttributeRegistry.FACADES.has(sourceIndex.value())
    }

    override fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta {
        val derivationRule = AttributeCoreTextMeta.DerivationRule(operationIndex, elementIndex)
        return AttributeCoreTextMeta(sourceIndex, sourceOrdinal, defaultText, derivationRule)
    }
}

internal data class SkillCoreTextMeta(
    override val sourceIndex: SourceIndex,
    override val sourceOrdinal: SourceOrdinal,
    override val defaultText: List<Component>?,
) : SimpleTextMeta {
    override val derivedIndexes: List<DerivedIndex> = deriveIndexes()

    override fun deriveIndexes(): List<DerivedIndex> {
        return listOf(sourceIndex)
    }
}

internal data class SkillCoreTextMetaFactory(
    override val namespace: String,
) : TextMetaFactory {
    override fun test(sourceIndex: SourceIndex): Boolean {
        // val key = Key.key(
        //     sourceIndex.value().substringBefore('/'),
        //     sourceIndex.value().substringAfter('/')
        // )
        // FIXME 临时方案, 理想中的技能 key 应该如上面注释所示
        //  也就是说, 如果 sourceIndex 是 skill:buff/potion_drop,
        //  那么对应的技能的 key 应该是 buff:potion_drop (???)

        return sourceIndex.namespace() == namespace && SkillRegistry.INSTANCES.has(sourceIndex)
    }

    override fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta {
        return SkillCoreTextMeta(sourceIndex, sourceOrdinal, defaultText)
    }
}