package cc.mewcraft.wakame.display2.implementation.standard

import cc.mewcraft.wakame.MM
import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.display2.implementation.common.*
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.player.attackspeed.AttackSpeedLevel
import cc.mewcraft.wakame.registry2.KoishRegistries
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.spongepowered.configurate.objectmapping.ConfigSerializable


@ConfigSerializable
internal data class CellularAttributeRendererFormat(
    override val namespace: String,
    private val ordinal: AttributeCoreOrdinalFormat,
) : RendererFormat.Dynamic<AttributeCore> {
    override val textMetaFactory: TextMetaFactory = AttributeCoreTextMetaFactory(namespace, ordinal.operation, ordinal.element)
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace) { id: String -> KoishRegistries.ATTRIBUTE_FACADE.containsId(id) }

    fun render(data: AttributeCore): IndexedText {
        return SimpleIndexedText(computeIndex(data), data.description)
    }

    /**
     * 实现要求: 返回值必须是 [AttributeCoreTextMeta.derivedIndexes] 的子集.
     */
    override fun computeIndex(data: AttributeCore): Key {
        return data.computeIndex(namespace)
    }
}

@ConfigSerializable
internal data class CellularEmptyRendererFormat(
    override val namespace: String,
    private val tooltip: List<Component>,
) : RendererFormat.Simple {
    override val id: String = "cells/empty"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = CyclicTextMetaFactory(namespace, id, CyclicIndexRule.SLASH)
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    private val tooltipCycle = IndexedTextCycle(limit = CyclicTextMeta.MAX_DISPLAY_COUNT) { i ->
        SimpleIndexedText(CyclicIndexRule.SLASH.make(index, i), tooltip)
    }

    fun render(data: EmptyCore): IndexedText {
        return tooltipCycle.next()
    }
}

@ConfigSerializable
internal data class AttackSpeedRendererFormat(
    override val namespace: String,
    private val tooltip: Tooltip = Tooltip(),
) : RendererFormat.Simple {
    override val id: String = "attack_speed"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = TextMetaFactory()
    override val textMetaPredicate: TextMetaFactoryPredicate = TextMetaFactoryPredicate(namespace, id)

    fun render(data: AttackSpeedLevel): IndexedText {
        val resolver = Placeholder.component("value", tooltip.level.getOrDefault(data.ordinal, UNKNOWN_LEVEL))
        return SimpleIndexedText(index, listOf(MM.deserialize(tooltip.line, resolver)))
    }

    @ConfigSerializable
    data class Tooltip(
        val line: String = "Attack Speed: <value>",
        val level: Map<Int, Component> = mapOf(
            0 to Component.text("Very Slow"),
            1 to Component.text("Slow"),
            2 to Component.text("Normal"),
            3 to Component.text("Fast"),
            4 to Component.text("Very Fast"),
            // 等攻速可自定义的时候, 这部分也要跟着重构一下
        ),
    )

    companion object Shared {
        private val UNKNOWN_LEVEL = Component.text("???")
    }
}