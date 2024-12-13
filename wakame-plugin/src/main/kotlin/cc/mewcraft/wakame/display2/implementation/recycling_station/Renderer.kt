package cc.mewcraft.wakame.display2.implementation.recycling_station

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.AbstractItemRenderer
import cc.mewcraft.wakame.display2.implementation.AbstractRendererFormatRegistry
import cc.mewcraft.wakame.display2.implementation.AbstractRendererLayout
import cc.mewcraft.wakame.display2.implementation.RenderingHandler
import cc.mewcraft.wakame.display2.implementation.RenderingHandlerRegistry
import cc.mewcraft.wakame.item.level
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.reforge.recycle.RecyclingSession
import cc.mewcraft.wakame.util.isClientSide
import cc.mewcraft.wakame.util.itemName
import cc.mewcraft.wakame.util.lore0
import cc.mewcraft.wakame.util.showNothing
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.inventory.ItemStack
import java.nio.file.Path


internal class RecyclingStationRendererFormatRegistry(renderer: RecyclingStationItemRenderer) : AbstractRendererFormatRegistry(renderer)

internal class RecyclingStationRendererLayout(renderer: RecyclingStationItemRenderer) : AbstractRendererLayout(renderer)

internal sealed interface RecyclingStationContext {

    sealed interface Items {
        val items: List<ItemStack>
    }

    sealed interface Result {
        val result: RecyclingSession.PurchaseResult
    }

    data object Empty : RecyclingStationContext

    data class Confirmed(
        override val items: List<ItemStack>,
        override val result: RecyclingSession.PurchaseResult,
    ) : RecyclingStationContext, Items, Result

    data class Unconfirmed(
        override val items: List<ItemStack>,
        override val result: RecyclingSession.PurchaseResult,
    ) : RecyclingStationContext, Items, Result

}

internal object RecyclingStationItemRenderer : AbstractItemRenderer<ItemStack, RecyclingStationContext>() {
    override val name: String = "recycling_station"
    override val formats = RecyclingStationRendererFormatRegistry(this)
    override val layout: AbstractRendererLayout = RecyclingStationRendererLayout(this)
    private val textAssembler: TextAssembler = TextAssembler(layout)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        RecyclingStationRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: ItemStack, context: RecyclingStationContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        // 渲染 `minecraft:item_name`
        val nameFmt = formats.get<SellButtonTitleRendererFormat>("sell_button_title") ?: error("format not found: sell_button_title")
        val itemName = when (context) {
            is RecyclingStationContext.Empty -> nameFmt.emptyInput.name
            is RecyclingStationContext.Confirmed -> nameFmt.confirmed.name
            is RecyclingStationContext.Unconfirmed -> nameFmt.unconfirmed.name
        }
        item.itemName = itemName

        // 渲染 `minecraft:lore`
        RecyclingStationRenderingHandlerRegistry.SELL_BUTTON_TITLE.process(collector, context)
        RecyclingStationRenderingHandlerRegistry.SELL_BUTTON_USAGE.process(collector, context)
        RecyclingStationRenderingHandlerRegistry.SELL_BUTTON_ITEM_LIST.process(collector, context)
        RecyclingStationRenderingHandlerRegistry.SELL_BUTTON_TOTAL_WORTH.process(collector, context)
        item.lore0 = textAssembler.assemble(collector)

        // 渲染其他可见数据
        item.showNothing()
    }
}

internal object RecyclingStationRenderingHandlerRegistry : RenderingHandlerRegistry(RecyclingStationItemRenderer) {
    @JvmField
    val SELL_BUTTON_TITLE: RenderingHandler<RecyclingStationContext, SellButtonTitleRendererFormat> = configure("sell_button_title") { context, format ->
        when (context) {
            is RecyclingStationContext.Empty -> SimpleIndexedText(format.index, format.emptyInput.lore)
            is RecyclingStationContext.Confirmed -> SimpleIndexedText(format.index, format.confirmed.lore)
            is RecyclingStationContext.Unconfirmed -> SimpleIndexedText(format.index, format.unconfirmed.lore)
        }
    }

    @JvmField
    val SELL_BUTTON_USAGE: RenderingHandler<RecyclingStationContext, SellButtonUsageRendererFormat> = configure("sell_button_usage") { context, format ->
        when (context) {
            is RecyclingStationContext.Empty -> SimpleIndexedText(format.index, format.emptyInput)
            is RecyclingStationContext.Confirmed -> SimpleIndexedText(format.index, format.confirmed)
            is RecyclingStationContext.Unconfirmed -> SimpleIndexedText(format.index, format.unconfirmed)
        }
    }

    @JvmField
    val SELL_BUTTON_ITEM_LIST: RenderingHandler<RecyclingStationContext, SellButtonItemListRendererFormat> = configure("sell_button_item_list") { context, format ->
        if (context !is RecyclingStationContext.Items) {
            return@configure IndexedText.NOP
        }

        val itemList = context.items.map { item ->
            val itemName = item.itemName ?: translatable(item)
            val itemLevel = item.shadowNeko()?.level?.let(::text)
            if (itemLevel != null) {
                MM.deserialize(
                    format.withLevel,
                    Placeholder.component("item_name", itemName),
                    Placeholder.component("item_level", itemLevel)
                )
            } else {
                MM.deserialize(
                    format.withoutLevel,
                    Placeholder.component("item_name", itemName)
                )
            }
        }

        SimpleIndexedText(format.index, itemList)
    }

    @JvmField
    val SELL_BUTTON_TOTAL_WORTH: RenderingHandler<RecyclingStationContext, SellButtonTotalWorthRendererFormat> = configure("sell_button_total_worth") { context, format ->
        if (context !is RecyclingStationContext.Result) {
            return@configure IndexedText.NOP
        }

        val result = context.result as? RecyclingSession.PurchaseResult.Success
        if (result == null) {
            return@configure IndexedText.NOP
        }

        val minWorth = result.minPrice
        val maxWorth = result.maxPrice
        val worthLines = format.totalWorth.map { line ->
            MM.deserialize(
                line,
                Formatter.number("min_worth", minWorth),
                Formatter.number("max_worth", maxWorth),
            )
        }

        SimpleIndexedText(format.index, worthLines)
    }
}
