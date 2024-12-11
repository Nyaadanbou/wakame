package cc.mewcraft.wakame.display2.implementation.standard

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.implementation.SingleSimpleTextMetaFactory
import cc.mewcraft.wakame.display2.implementation.common.AttributeCoreOrdinalFormat
import cc.mewcraft.wakame.display2.implementation.common.CyclicIndexRule
import cc.mewcraft.wakame.display2.implementation.common.CyclicTextMeta
import cc.mewcraft.wakame.display2.implementation.common.CyclicTextMetaFactory
import cc.mewcraft.wakame.display2.implementation.common.IndexedTextCycle
import cc.mewcraft.wakame.display2.implementation.common.computeIndex
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.item.components.cells.SkillCore
import cc.mewcraft.wakame.player.attackspeed.AttackSpeedLevel
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting


@ConfigSerializable
internal data class CellularAttributeRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @Required
    private val ordinal: AttributeCoreOrdinalFormat,
) : RendererFormat.Dynamic<AttributeCore> {
    override val textMetaFactory = AttributeCoreTextMetaFactory(namespace, ordinal.operation, ordinal.element)

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
internal data class CellularSkillRendererFormat(
    @Setting @Required
    override val namespace: String,
) : RendererFormat.Dynamic<SkillCore> {
    override val textMetaFactory = SkillCoreTextMetaFactory(namespace)

    fun render(data: SkillCore): IndexedText {
        val instance = data.skill.instance
        val tooltip = instance.displays.tooltips.map(MM::deserialize)
        return SimpleIndexedText(computeIndex(data), tooltip)
    }

    override fun computeIndex(data: SkillCore): Key {
        val dataId = data.skill.id
        val indexId = dataId.namespace() + "/" + dataId.value()
        return Key.key(namespace, indexId)
    }

    companion object Shared {
        private val MM = Injector.get<MiniMessage>()
    }
}

@ConfigSerializable
internal data class CellularEmptyRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting
    private val tooltip: List<Component> = listOf(Component.text("Empty Slot")),
) : RendererFormat.Simple {
    override val id = "cells/empty"
    override val index = createIndex()

    private val cyclicIndexRule = CyclicIndexRule.SLASH
    override val textMetaFactory = CyclicTextMetaFactory(namespace, id, cyclicIndexRule)

    private val tooltipCycle = IndexedTextCycle(limit = CyclicTextMeta.MAX_DISPLAY_COUNT) { i ->
        SimpleIndexedText(cyclicIndexRule.make(index, i), tooltip)
    }

    fun render(data: EmptyCore): IndexedText {
        return tooltipCycle.next()
    }
}

@ConfigSerializable
internal data class AttackSpeedRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting
    private val tooltip: Tooltip = Tooltip(),
) : RendererFormat.Simple {
    override val id = "attack_speed"
    override val index = createIndex()
    override val textMetaFactory = SingleSimpleTextMetaFactory(namespace, id)

    fun render(data: AttackSpeedLevel): IndexedText {
        val resolver = Placeholder.component("value", tooltip.level.getOrDefault(data.ordinal, UNKNOWN_LEVEL))
        return SimpleIndexedText(index, listOf(MM.deserialize(tooltip.line, resolver)))
    }

    @ConfigSerializable
    data class Tooltip(
        @Setting
        val line: String = "Attack Speed: <value>",
        @Setting
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
        private val MM = Injector.get<MiniMessage>()
        private val UNKNOWN_LEVEL = Component.text("???")
    }
}