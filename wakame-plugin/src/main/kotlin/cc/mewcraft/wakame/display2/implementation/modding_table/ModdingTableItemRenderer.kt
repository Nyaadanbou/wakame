/**
 * 有关*定制台*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation.modding_table

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.AbstractItemRenderer
import cc.mewcraft.wakame.display2.implementation.AbstractRendererFormats
import cc.mewcraft.wakame.display2.implementation.AbstractRendererLayout
import cc.mewcraft.wakame.display2.implementation.AggregateValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.HardcodedRendererFormat
import cc.mewcraft.wakame.display2.implementation.RenderingPart
import cc.mewcraft.wakame.display2.implementation.RenderingPart2
import cc.mewcraft.wakame.display2.implementation.RenderingPart3
import cc.mewcraft.wakame.display2.implementation.RenderingParts
import cc.mewcraft.wakame.display2.implementation.SingleValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.AttributeCoreOrdinalFormat
import cc.mewcraft.wakame.display2.implementation.common.CommonRenderingParts
import cc.mewcraft.wakame.display2.implementation.common.CyclicIndexRule
import cc.mewcraft.wakame.display2.implementation.common.CyclicTextMeta
import cc.mewcraft.wakame.display2.implementation.common.CyclicTextMetaFactory
import cc.mewcraft.wakame.display2.implementation.common.DifferenceFormat
import cc.mewcraft.wakame.display2.implementation.common.IndexedTextCycle
import cc.mewcraft.wakame.display2.implementation.common.RarityRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.StandaloneCellRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.computeIndex
import cc.mewcraft.wakame.display2.implementation.standard.AttributeCoreTextMeta
import cc.mewcraft.wakame.display2.implementation.standard.AttributeCoreTextMetaFactory
import cc.mewcraft.wakame.display2.implementation.standard.SkillCoreTextMetaFactory
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.ReforgeHistory
import cc.mewcraft.wakame.item.components.StandaloneCell
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.item.components.cells.SkillCore
import cc.mewcraft.wakame.item.reforgeHistory
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.CustomName
import cc.mewcraft.wakame.item.templates.components.ItemName
import cc.mewcraft.wakame.item.unsafeEdit
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.reforge.mod.ModdingSession
import cc.mewcraft.wakame.util.removeItalic
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.nio.file.Path

internal class ModdingTableRendererFormats(renderer: ModdingTableItemRenderer) : AbstractRendererFormats(renderer)

internal class ModdingTableRendererLayout(renderer: ModdingTableItemRenderer) : AbstractRendererLayout(renderer)

internal sealed interface ModdingTableContext {
    val session: ModdingSession

    /**
     * 用于输入的主要物品, 也就是需要被定制的物品.
     */
    data class Input(override val session: ModdingSession) : ModdingTableContext

    /**
     * 用于输出的主要物品, 也就是经过定制后的物品.
     */
    data class Output(override val session: ModdingSession) : ModdingTableContext

    /**
     * 用于核孔的预览, 例如渲染核孔的名字, 重铸的历史数据等.
     */
    data class Preview(override val session: ModdingSession) : ModdingTableContext

    /**
     * 用于便携式核心.
     */
    data class Replace(override val session: ModdingSession, val replace: ModdingSession.Replace) : ModdingTableContext
}

internal object ModdingTableItemRenderer : AbstractItemRenderer<NekoStack, ModdingTableContext>() {
    override val name: String = "modding_table"
    override val formats = ModdingTableRendererFormats(this)
    override val layout = ModdingTableRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        ModdingTableRendererParts.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: NekoStack, context: ModdingTableContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        val templates = item.templates
        templates.process(ItemTemplateTypes.CUSTOM_NAME) { data -> ModdingTableRendererParts.CUSTOM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.ITEM_NAME) { data -> ModdingTableRendererParts.ITEM_NAME.process(collector, data) }

        val components = item.components
        components.process(ItemComponentTypes.ELEMENTS) { data -> ModdingTableRendererParts.ELEMENTS.process(collector, data) }
        components.process(ItemComponentTypes.LEVEL) { data -> ModdingTableRendererParts.LEVEL.process(collector, data) }
        components.process(ItemComponentTypes.RARITY, ItemComponentTypes.REFORGE_HISTORY) { data1, data2 ->
            val data1 = data1 ?: return@process
            val data2 = data2 ?: ReforgeHistory.ZERO
            ModdingTableRendererParts.RARITY.process(collector, data1, data2)
        }

        if (context is ModdingTableContext.Input) {
            components.process(ItemComponentTypes.CELLS) { data ->
                for ((_, cell) in data) when (val core = cell.getCore()) {
                    is AttributeCore -> ModdingTableRendererParts.CELLULAR_ATTRIBUTE_MAIN_IN.process(collector, cell.getId(), core, context)
                    is SkillCore -> ModdingTableRendererParts.CELLULAR_SKILL_IN.process(collector, cell.getId(), core, context)
                    is EmptyCore -> ModdingTableRendererParts.CELLULAR_EMPTY_IN.process(collector, cell.getId(), context)
                }
            }
        }

        if (context is ModdingTableContext.Output) {
            components.process(ItemComponentTypes.CELLS) { data ->
                for ((_, cell) in data) when (val core = cell.getCore()) {
                    is AttributeCore -> ModdingTableRendererParts.CELLULAR_ATTRIBUTE_MAIN_OUT.process(collector, cell.getId(), core, context)
                    is SkillCore -> ModdingTableRendererParts.CELLULAR_SKILL_OUT.process(collector, cell.getId(), core, context)
                    is EmptyCore -> ModdingTableRendererParts.CELLULAR_EMPTY_OUT.process(collector, cell.getId(), context)
                }
            }

            // 输出物品需要渲染定制花费
            ModdingTableRendererParts.REFORGE_COST.process(collector, context)
        }

        if (context is ModdingTableContext.Preview) {
            components.process(ItemComponentTypes.STANDALONE_CELL) { data -> ModdingTableRendererParts.STANDALONE_CELL.process(collector, item, data, context) }
        }

        if (context is ModdingTableContext.Replace) {
            val augment = context.replace.augment
            if (augment != null) {
                ModdingTableRendererParts.REPLACE_IN.process(collector, augment, context)
            }
        }

        val itemLore = textAssembler.assemble(collector)
        val itemCustomModelData = ItemModelDataLookup[item.id, item.variant]

        item.erase()

        item.unsafeEdit {
            lore = itemLore
            customModelData = itemCustomModelData
            showNothing()
        }
    }
}


//////


internal object ModdingTableRendererParts : RenderingParts(ModdingTableItemRenderer) {

    //<editor-fold desc="重铸部分">
    @JvmField
    val CELLULAR_ATTRIBUTE_MAIN_IN: RenderingPart3<String, AttributeCore, ModdingTableContext, CellularAttributeRendererFormat> = configure3("cells/attributes/in") { id, attribute, context, format ->
        format.render(id, attribute, context)
    }

    @JvmField
    val CELLULAR_ATTRIBUTE_MAIN_OUT: RenderingPart3<String, AttributeCore, ModdingTableContext, CellularAttributeRendererFormat> = configure3("cells/attributes/out") { id, attribute, context, format ->
        format.render(id, attribute, context)
    }

    @JvmField
    val CELLULAR_SKILL_IN: RenderingPart3<String, SkillCore, ModdingTableContext, CellularSkillRendererFormat> = configure3("cells/skills/in") { id, skill, context, format ->
        format.render(id, skill, context)
    }

    @JvmField
    val CELLULAR_SKILL_OUT: RenderingPart3<String, SkillCore, ModdingTableContext, CellularSkillRendererFormat> = configure3("cells/skills/out") { id, skill, context, format ->
        format.render(id, skill, context)
    }

    @JvmField
    val CELLULAR_EMPTY_IN: RenderingPart2<String, ModdingTableContext, CellularEmptyRendererFormat> = configure2("cells/empty/in") { id, context, format ->
        format.render(id, context)
    }

    @JvmField
    val CELLULAR_EMPTY_OUT: RenderingPart2<String, ModdingTableContext, CellularEmptyRendererFormat> = configure2("cells/empty/out") { id, context, format ->
        format.render(id, context)
    }

    @JvmField
    val REPLACE_IN: RenderingPart2<PortableCore, ModdingTableContext, HardcodedRendererFormat> = configure2("replace_input") { core, context, format ->
        SimpleIndexedText(format.index, core.description)
    }

    @JvmField
    val STANDALONE_CELL: RenderingPart3<NekoStack, StandaloneCell, ModdingTableContext, StandaloneCellRendererFormat> = configure3("standalone_cell") { item, cell, context, format ->
        val coreText = cell.core.description
        val modCount = item.reforgeHistory.modCount
        val modLimit = context.session.itemRule?.modLimit ?: 0
        format.render(coreText, modCount, modLimit)
    }

    @JvmField
    val REFORGE_COST: RenderingPart<ModdingTableContext, HardcodedRendererFormat> = configure("reforge_cost") { context, format ->
        SimpleIndexedText(format.index, context.session.latestResult.reforgeCost.description.removeItalic)
    }
    //</editor-fold>

    //<editor-fold desc="通用部分">
    @JvmField
    val CUSTOM_NAME: RenderingPart<CustomName, SingleValueRendererFormat> = CommonRenderingParts.CUSTOM_NAME(this)

    @JvmField
    val ELEMENTS: RenderingPart<ItemElements, AggregateValueRendererFormat> = CommonRenderingParts.ELEMENTS(this)

    @JvmField
    val ITEM_NAME: RenderingPart<ItemName, SingleValueRendererFormat> = CommonRenderingParts.ITEM_NAME(this)

    @JvmField
    val LEVEL: RenderingPart<ItemLevel, SingleValueRendererFormat> = CommonRenderingParts.LEVEL(this)

    @JvmField
    val RARITY: RenderingPart2<ItemRarity, ReforgeHistory, RarityRendererFormat> = CommonRenderingParts.RARITY(this)
    //</editor-fold>
}


//////


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
