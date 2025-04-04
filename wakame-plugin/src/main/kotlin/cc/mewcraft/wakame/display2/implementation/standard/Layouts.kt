package cc.mewcraft.wakame.display2.implementation.standard

import cc.mewcraft.wakame.attribute.bundle.AttributeBundleTrait
import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.entity.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.KoishRegistries2
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
            addList(derivation.elementIndex, KoishRegistries.ATTRIBUTE_FACADE.getOrThrow(sourceId).bundleTrait.has<AttributeBundleTrait.Element>())
        }
        val combinations = combiner.combine()
        return combinations.map { Key.key(sourceNamespace, it) }
    }

    data class DerivationRule(
        val operationIndex: List<String>,
        val elementIndex: List<String>,
    ) {
        init { // validate values
            this.operationIndex.forEach { Operation.byName(it) ?: error("'$it' is not a valid attribute modifier operation, check your renderer config") }
            this.elementIndex.forEach { if (!KoishRegistries2.ELEMENT.containsId(it)) error("'$it' is not a valid element type, check your renderer config") }
        }
    }
}

internal data class AttributeCoreTextMetaFactory(
    private val namespace: String,
    private val operationIndex: List<String>,
    private val elementIndex: List<String>,
) : TextMetaFactory {
    override fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta {
        val derivationRule = AttributeCoreTextMeta.DerivationRule(operationIndex, elementIndex)
        return AttributeCoreTextMeta(sourceIndex, sourceOrdinal, defaultText, derivationRule)
    }
}

internal data class AbilityCoreTextMeta(
    override val sourceIndex: SourceIndex,
    override val sourceOrdinal: SourceOrdinal,
    override val defaultText: List<Component>?,
) : SimpleTextMeta {
    override val derivedIndexes: List<DerivedIndex> = deriveIndexes()

    override fun deriveIndexes(): List<DerivedIndex> {
        return listOf(sourceIndex)
    }
}

internal data class AbilityCoreTextMetaFactory(
    private val namespace: String,
) : TextMetaFactory {
    override fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta {
        return AbilityCoreTextMeta(sourceIndex, sourceOrdinal, defaultText)
    }
}