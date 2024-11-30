/**
 * 有关*合并台*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation.merging_table

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.RendererFormat
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.TextMetaFactory
import cc.mewcraft.wakame.display2.implementation.AbstractItemRenderer
import cc.mewcraft.wakame.display2.implementation.AbstractRendererFormats
import cc.mewcraft.wakame.display2.implementation.AbstractRendererLayout
import cc.mewcraft.wakame.display2.implementation.AggregateValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.RenderingPart
import cc.mewcraft.wakame.display2.implementation.RenderingPart2
import cc.mewcraft.wakame.display2.implementation.RenderingParts
import cc.mewcraft.wakame.display2.implementation.SingleSimpleTextMetaFactory
import cc.mewcraft.wakame.display2.implementation.SingleValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.CommonRenderingParts
import cc.mewcraft.wakame.display2.implementation.common.RarityRendererFormat
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemElements
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.ReforgeHistory
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.CustomName
import cc.mewcraft.wakame.item.templates.components.ItemName
import cc.mewcraft.wakame.item.unsafeEdit
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.reforge.merge.MergingSession
import cc.mewcraft.wakame.util.EnumLookup
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.NodeKey
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.nio.file.Path
import java.util.regex.Pattern

internal class MergingTableRendererFormats(renderer: MergingTableItemRenderer) : AbstractRendererFormats(renderer)

internal class MergingTableRendererLayout(renderer: MergingTableItemRenderer) : AbstractRendererLayout(renderer)

internal sealed interface MergingTableContext {
    data class MergeInputSlot(val session: MergingSession) : MergingTableContext
    data class MergeOutputSlot(val session: MergingSession) : MergingTableContext
}

internal object MergingTableItemRenderer : AbstractItemRenderer<NekoStack, MergingTableContext>() {
    override val name: String = "merging_table"
    override val formats = MergingTableRendererFormats(this)
    override val layout = MergingTableRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        MergingTableRenderingParts.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: NekoStack, context: MergingTableContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        val templates = item.templates
        templates.process(ItemTemplateTypes.CUSTOM_NAME) { data -> MergingTableRenderingParts.CUSTOM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.ITEM_NAME) { data -> MergingTableRenderingParts.ITEM_NAME.process(collector, data) }

        val components = item.components
        components.process(ItemComponentTypes.ELEMENTS) { data -> MergingTableRenderingParts.ELEMENTS.process(collector, data) }
        components.process(ItemComponentTypes.LEVEL) { data -> MergingTableRenderingParts.LEVEL.process(collector, data) }
        components.process(ItemComponentTypes.RARITY, ItemComponentTypes.REFORGE_HISTORY) { data1, data2 ->
            val data1 = data1 ?: return@process
            val data2 = data2 ?: ReforgeHistory.ZERO
            MergingTableRenderingParts.RARITY.process(collector, data1, data2)
        }
        components.process(ItemComponentTypes.PORTABLE_CORE) { data ->
            when (context) {
                is MergingTableContext.MergeInputSlot -> MergingTableRenderingParts.MERGE_IN.process(collector, data, context)
                is MergingTableContext.MergeOutputSlot -> MergingTableRenderingParts.MERGE_OUT.process(collector, data, context)
            }
        }

        val itemLore = textAssembler.assemble(collector)
        val itemCustomModelData = ItemModelDataLookup[item.id, item.variant]

        item.erase()

        item.unsafeEdit {
            // 本 ItemRenderer 专门渲染放在菜单里面的物品,
            // 而这些物品有些时候会被玩家(用铁砧)修改 `minecraft:custom_name`
            // 导致在菜单里显示的是玩家自己设置的(奇葩)名字.
            // 我们在这里统一清除掉这个组件.
            customName = null

            lore = itemLore
            customModelData = itemCustomModelData
            showNothing()
        }
    }
}

internal object MergingTableRenderingParts : RenderingParts(MergingTableItemRenderer) {
    @JvmField
    val CUSTOM_NAME: RenderingPart<CustomName, SingleValueRendererFormat> = CommonRenderingParts.CUSTOM_NAME(this)

    @JvmField
    val ELEMENTS: RenderingPart<ItemElements, AggregateValueRendererFormat> = CommonRenderingParts.ELEMENTS(this)

    @JvmField
    val ITEM_NAME: RenderingPart<ItemName, SingleValueRendererFormat> = CommonRenderingParts.ITEM_NAME(this)

    @JvmField
    val LEVEL: RenderingPart<ItemLevel, SingleValueRendererFormat> = CommonRenderingParts.LEVEL(this)

    // 渲染放在输入容器的便携核心
    @JvmField
    val MERGE_IN: RenderingPart2<PortableCore, MergingTableContext.MergeInputSlot, MergeInputOutputRendererFormat> = configure2("merge_input") { data, context, format ->
        format.render(data, context)
    }

    // 渲染放在输出容器的便携核心
    @JvmField
    val MERGE_OUT: RenderingPart2<PortableCore, MergingTableContext.MergeOutputSlot, MergeInputOutputRendererFormat> = configure2("merge_output") { data, context, format ->
        format.render(data, context)
    }

    @JvmField
    val RARITY: RenderingPart2<ItemRarity, ReforgeHistory, RarityRendererFormat> = CommonRenderingParts.RARITY(this)
}


//////


@ConfigSerializable
internal data class MergeInputOutputRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting @NodeKey
    override val id: String,
    @Setting("penalty_format")
    private val penaltyFormat: String = "Penalty: <penalty>",
    @Setting("overall_ordinal") @Required
    private val _unprocessedOrdinal: List<String>,
    @Setting
    private val obfuscation: Obfuscation = Obfuscation(Pattern.compile(""), empty()),
) : RendererFormat.Simple {
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = SingleSimpleTextMetaFactory(namespace, id)

    private val ordinal = _unprocessedOrdinal.mapNotNull { raw -> EnumLookup.lookup<OrdinalIndex>(raw).getOrNull() }

    fun render(core: PortableCore, context: MergingTableContext.MergeInputSlot): IndexedText {
        // 暂时用不到 context, 之后再看看
        val text = buildList {
            ordinal.forEach {
                when (it) {
                    OrdinalIndex.CORE ->
                        addAll(core.description)

                    OrdinalIndex.PENALTY ->
                        add(renderPenalty(core))
                }
            }
        }
        return SimpleIndexedText(index, text)
    }

    fun render(core: PortableCore, context: MergingTableContext.MergeOutputSlot): IndexedText {
        val text = buildList {
            ordinal.forEach {
                when (it) {
                    OrdinalIndex.CORE -> {
                        addAll(core.description.map { line ->
                            line.replaceText(obfuscation.textReplacementConfig)
                        })
                    }

                    OrdinalIndex.PENALTY ->
                        add(renderPenalty(core))
                }
            }
        }
        return SimpleIndexedText(index, text)
    }

    private fun renderPenalty(core: PortableCore): Component {
        return MM.deserialize(
            penaltyFormat,
            Formatter.number("penalty", core.penalty)
        )
    }

    enum class OrdinalIndex {
        CORE, PENALTY
    }

    @ConfigSerializable
    data class Obfuscation(
        @Setting @Required
        val pattern: Pattern,
        @Setting @Required
        val replacement: Component,
    ) {
        val textReplacementConfig: TextReplacementConfig = TextReplacementConfig.builder()
            .match(pattern)
            .replacement(replacement)
            .build()
    }

    companion object {
        private val MM = Injector.get<MiniMessage>()
    }
}