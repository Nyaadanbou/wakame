/**
 * 有关*定制台*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation.modding_table

import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.display2.implementation.*
import cc.mewcraft.wakame.display2.implementation.common.*
import cc.mewcraft.wakame.display2.implementation.standard.*
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.*
import cc.mewcraft.wakame.item.components.cells.*
import cc.mewcraft.wakame.item.directEdit
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.CustomName
import cc.mewcraft.wakame.item.templates.components.ItemName
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.reforge.mod.ModdingSession
import cc.mewcraft.wakame.util.colorRecursively
import cc.mewcraft.wakame.util.removeItalic
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.nio.file.Path

internal class ModdingTableRendererFormats(renderer: ModdingTableItemRenderer) : AbstractRendererFormats(renderer)

internal class ModdingTableRendererLayout(renderer: ModdingTableItemRenderer) : AbstractRendererLayout(renderer)

internal sealed interface ModdingTableContext {
    // 输入的物品 (需要被定制的物品)
    data class MainInputSlot(val session: ModdingSession) : ModdingTableContext

    // 输出的物品 (经过定制后的物品)
    data class MainOutputSlot(val session: ModdingSession) : ModdingTableContext

    // 核孔的预览
    data class ReplacePreview(val session: ModdingSession) : ModdingTableContext

    // 玩家放入的核心
    data class ReplaceInputSlot(val replace: ModdingSession.Replace) : ModdingTableContext
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
        components.process(ItemComponentTypes.RARITY) { data -> ModdingTableRendererParts.RARITY.process(collector, data) }

        if (context is ModdingTableContext.ReplacePreview) {
            components.process(ItemComponentTypes.STANDALONE_CELL) { data -> ModdingTableRendererParts.STANDALONE_CELL.process(collector, data, context) }
        }

        if (context is ModdingTableContext.MainInputSlot) {
            components.process(ItemComponentTypes.CELLS) { data ->
                for ((_, cell) in data) when (val core = cell.getCore()) {
                    is AttributeCore -> ModdingTableRendererParts.CELLULAR_ATTRIBUTE_MAIN_IN.process(collector, cell.getId(), core, context)
                    is SkillCore -> ModdingTableRendererParts.CELLULAR_SKILL_IN.process(collector, cell.getId(), core, context)
                    is EmptyCore -> ModdingTableRendererParts.CELLULAR_EMPTY_IN.process(collector, cell.getId(), context)
                }
            }
        }

        if (context is ModdingTableContext.MainOutputSlot) {
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

        if (context is ModdingTableContext.ReplaceInputSlot) {
            val replace = context.replace
            val augment = replace.latestResult.augment
            if (augment != null) {
                ModdingTableRendererParts.REPLACE_IN.process(collector, augment, context)
            }
        }

        val itemLore = textAssembler.assemble(collector)
        val itemCustomModelData = ItemModelDataLookup[item.id, item.variant]

        item.erase()

        item.directEdit {
            lore = itemLore
            customModelData = itemCustomModelData
            showNothing()
        }
    }
}


//////


internal object ModdingTableRendererParts : RenderingParts(ModdingTableItemRenderer) {
    @JvmField
    val CELLULAR_ATTRIBUTE_MAIN_IN: RenderingPart3<String, AttributeCore, ModdingTableContext.MainInputSlot, CellularAttributeRendererFormat> = configure3("cells/attributes/in") { id, attribute, inputSlot, format ->
        format.render(id, attribute, inputSlot)
    }

    @JvmField
    val CELLULAR_ATTRIBUTE_MAIN_OUT: RenderingPart3<String, AttributeCore, ModdingTableContext.MainOutputSlot, CellularAttributeRendererFormat> = configure3("cells/attributes/out") { id, attribute, outputSlot, format ->
        format.render(id, attribute, outputSlot)
    }

    @JvmField
    val CELLULAR_SKILL_IN: RenderingPart3<String, SkillCore, ModdingTableContext.MainInputSlot, CellularSkillRendererFormat> = configure3("cells/skills/in") { id, skill, inputSlot, format ->
        format.render(id, skill, inputSlot)
    }

    @JvmField
    val CELLULAR_SKILL_OUT: RenderingPart3<String, SkillCore, ModdingTableContext.MainOutputSlot, CellularSkillRendererFormat> = configure3("cells/skills/out") { id, skill, outputSlot, format ->
        format.render(id, skill, outputSlot)
    }

    @JvmField
    val CELLULAR_EMPTY_IN: RenderingPart2<String, ModdingTableContext.MainInputSlot, CellularEmptyRendererFormat> = configure2("cells/empty/in") { id, inputSlot, format ->
        format.render(id, inputSlot)
    }

    @JvmField
    val CELLULAR_EMPTY_OUT: RenderingPart2<String, ModdingTableContext.MainOutputSlot, CellularEmptyRendererFormat> = configure2("cells/empty/out") { id, outputSlot, format ->
        format.render(id, outputSlot)
    }

    @JvmField
    val REFORGE_COST: RenderingPart<ModdingTableContext.MainOutputSlot, HardcodedRendererFormat> = configure("reforge_cost") { context, format ->
        SimpleIndexedText(format.index, context.session.latestResult.reforgeCost.description.removeItalic)
    }

    @JvmField
    val REPLACE_IN: RenderingPart2<PortableCore, ModdingTableContext.ReplaceInputSlot, HardcodedRendererFormat> = configure2("replace_input") { core, context, format ->
        SimpleIndexedText(format.index, core.description)
    }

    @JvmField
    val STANDALONE_CELL: RenderingPart2<StandaloneCell, ModdingTableContext.ReplacePreview, StandaloneCellRendererFormat> = configure2("standalone_cell") { cell, context, format ->
        val replaceParams = context.session.replaceParams
        val penaltyLimit = replaceParams[cell.id]?.rule?.modLimit ?: 0
        format.render(cell, modPenaltyLimit = penaltyLimit)
    }

    @JvmField
    val CUSTOM_NAME: RenderingPart<CustomName, SingleValueRendererFormat> = CommonRenderingParts.CUSTOM_NAME(this)

    @JvmField
    val ELEMENTS: RenderingPart<ItemElements, AggregateValueRendererFormat> = CommonRenderingParts.ELEMENTS(this)

    @JvmField
    val ITEM_NAME: RenderingPart<ItemName, SingleValueRendererFormat> = CommonRenderingParts.ITEM_NAME(this)

    @JvmField
    val LEVEL: RenderingPart<ItemLevel, SingleValueRendererFormat> = CommonRenderingParts.LEVEL(this)

    @JvmField
    val RARITY: RenderingPart<ItemRarity, SingleValueRendererFormat> = CommonRenderingParts.RARITY(this)
}


//////


private fun renderMainInputSlot(text: List<Component>, replace: ModdingSession.Replace): List<Component> {
    var result = text

    // 核孔不可修改, 渲染成深灰色
    if (!replace.changeable) {
        result = result.map { it.colorRecursively(NamedTextColor.DARK_GRAY) }
    }

    // 核孔存在修改, 渲染成深灰色+删除线
    if (replace.hasInput) {
        result = result.map { it.decorate(TextDecoration.STRIKETHROUGH) }
    }

    return result
}

private fun renderMainOutputSlot(text: List<Component>, replace: ModdingSession.Replace): List<Component> {
    var result = text

    // 不可修改 或 没有输入, 则将属性的颜色变为深灰色
    if (!replace.changeable || !replace.hasInput) {
        result = result.map { it.colorRecursively(NamedTextColor.DARK_GRAY) }
    }

    return result
}

@ConfigSerializable
internal data class CellularAttributeRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @Required
    private val ordinal: Ordinal,
) : RendererFormat.Dynamic<AttributeCore> {
    override val textMetaFactory = AttributeCoreTextMetaFactory(namespace, ordinal.operation, ordinal.element)

    /**
     * @param id 核孔的 id
     */
    fun render(id: String, core: AttributeCore, context: ModdingTableContext.MainInputSlot): IndexedText {
        val replace = context.session.replaceParams[id] ?: error("Missing replace params for cell $id")

        val original = core.description
        val processed = renderMainInputSlot(original, replace)

        return SimpleIndexedText(computeIndex(core), processed)
    }

    /**
     * @param id 核孔的 id
     */
    fun render(id: String, core: AttributeCore, context: ModdingTableContext.MainOutputSlot): IndexedText {
        val replace = context.session.replaceParams[id] ?: error("Missing replace params for cell $id")

        val original = core.description
        val processed = renderMainOutputSlot(original, replace)

        return SimpleIndexedText(computeIndex(core), processed)
    }

    /**
     * 实现要求: 返回值必须是 [AttributeCoreTextMeta.derivedIndexes] 的子集.
     */
    override fun computeIndex(data: AttributeCore): Key {
        return data.computeIndex(namespace)
    }

    @ConfigSerializable
    data class Ordinal(
        @Setting @Required
        val element: List<String>,
        @Setting @Required
        val operation: List<String>,
    )
}

@ConfigSerializable
internal data class CellularSkillRendererFormat(
    @Setting @Required
    override val namespace: String,
) : RendererFormat.Dynamic<SkillCore> {
    override val textMetaFactory = SkillCoreTextMetaFactory(namespace)

    fun render(id: String, core: SkillCore, context: ModdingTableContext.MainInputSlot): IndexedText {
        val replace = context.session.replaceParams[id] ?: error("Missing replace params for cell $id")

        val original = core.description
        val processed = renderMainInputSlot(original, replace)

        return SimpleIndexedText(computeIndex(core), processed)
    }

    fun render(id: String, core: SkillCore, context: ModdingTableContext.MainOutputSlot): IndexedText {
        val replace = context.session.replaceParams[id] ?: error("Missing replace params for cell $id")

        val original = core.description
        val processed = renderMainOutputSlot(original, replace)

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
) : RendererFormat.Simple {
    override val id = "cells/empty"
    override val index = createIndex()

    private val cyclicIndexRule = CyclicIndexRule.SLASH
    override val textMetaFactory = CyclicTextMetaFactory(namespace, id, cyclicIndexRule)

    private val tooltipCycle = IndexedTextCycle(limit = CyclicTextMeta.MAX_DISPLAY_COUNT) { i ->
        SimpleIndexedText(cyclicIndexRule.make(index, i), tooltip)
    }

    fun render(id: String, context: ModdingTableContext.MainInputSlot): IndexedText {
        val replace = context.session.replaceParams[id] ?: error("Missing replace params for cell $id")

        val original = tooltipCycle.next()
        val processed = renderMainInputSlot(original.text, replace)

        return original.copy(text = processed)
    }

    fun render(id: String, context: ModdingTableContext.MainOutputSlot): IndexedText {
        val replace = context.session.replaceParams[id] ?: error("Missing replace params for cell $id")

        val original = tooltipCycle.next()
        val processed = renderMainOutputSlot(original.text, replace)

        return original.copy(text = processed)
    }
}
