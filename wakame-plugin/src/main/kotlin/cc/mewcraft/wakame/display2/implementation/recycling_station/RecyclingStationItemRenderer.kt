/**
 * 有关*收购站*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation.recycling_station

import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.display2.implementation.*
import cc.mewcraft.wakame.item.level
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.reforge.recycle.RecyclingSession
import cc.mewcraft.wakame.util.*
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import java.nio.file.Path

internal class RecyclingStationRendererFormats(renderer: RecyclingStationItemRenderer) : AbstractRendererFormats(renderer)
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
    override val formats = RecyclingStationRendererFormats(this)
    override val layout: AbstractRendererLayout = RecyclingStationRendererLayout(this)
    private val textAssembler: TextAssembler = TextAssembler(layout)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        RecyclingStationItemRendererParts.bootstrap()
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
        RecyclingStationItemRendererParts.SELL_BUTTON_TITLE.process(collector, context)
        RecyclingStationItemRendererParts.SELL_BUTTON_USAGE.process(collector, context)
        RecyclingStationItemRendererParts.SELL_BUTTON_ITEM_LIST.process(collector, context)
        RecyclingStationItemRendererParts.SELL_BUTTON_TOTAL_WORTH.process(collector, context)
        item.lore0 = textAssembler.assemble(collector)

        // 渲染其他可见数据
        item.showNothing()
    }
}

internal object RecyclingStationItemRendererParts : RenderingParts(RecyclingStationItemRenderer) {
    @JvmField
    val SELL_BUTTON_TITLE: RenderingPart<RecyclingStationContext, SellButtonTitleRendererFormat> = configure("sell_button_title") { context, format ->
        when (context) {
            is RecyclingStationContext.Empty -> SimpleIndexedText(format.index, format.emptyInput.lore)
            is RecyclingStationContext.Confirmed -> SimpleIndexedText(format.index, format.confirmed.lore)
            is RecyclingStationContext.Unconfirmed -> SimpleIndexedText(format.index, format.unconfirmed.lore)
        }
    }

    @JvmField
    val SELL_BUTTON_USAGE: RenderingPart<RecyclingStationContext, SellButtonUsageRendererFormat> = configure("sell_button_usage") { context, format ->
        when (context) {
            is RecyclingStationContext.Empty -> SimpleIndexedText(format.index, format.emptyInput)
            is RecyclingStationContext.Confirmed -> SimpleIndexedText(format.index, format.confirmed)
            is RecyclingStationContext.Unconfirmed -> SimpleIndexedText(format.index, format.unconfirmed)
        }
    }

    @JvmField
    val SELL_BUTTON_ITEM_LIST: RenderingPart<RecyclingStationContext, SellButtonItemListRendererFormat> = configure("sell_button_item_list") { context, format ->
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
    val SELL_BUTTON_TOTAL_WORTH: RenderingPart<RecyclingStationContext, SellButtonTotalWorthRendererFormat> = configure("sell_button_total_worth") { context, format ->
        if (context !is RecyclingStationContext.Result) {
            return@configure IndexedText.NOP
        }

        val result = context.result as? RecyclingSession.PurchaseResult.Success
        if (result == null) {
            return@configure IndexedText.NOP
        }

        val minWorth = text(result.minPrice)
        val maxWorth = text(result.maxPrice)
        val worthLines = format.totalWorth.map { line ->
            MM.deserialize(
                line,
                Placeholder.component("min_worth", minWorth),
                Placeholder.component("max_worth", maxWorth)
            )
        }

        SimpleIndexedText(format.index, worthLines)
    }
}

@ConfigSerializable
internal data class SellButtonTitleRendererFormat(
    @Required
    override val namespace: String,
    @Required
    val emptyInput: Part,
    @Required
    val unconfirmed: Part,
    @Required
    val confirmed: Part,
) : RendererFormat.Simple {
    override val id: String = "sell_button_title"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = SingleSimpleTextMetaFactory(namespace, id)

    @ConfigSerializable
    data class Part(
        val name: Component,
        val lore: List<Component> = emptyList(),
    )
}

@ConfigSerializable
internal data class SellButtonUsageRendererFormat(
    @Required
    override val namespace: String,
    @Required
    val emptyInput: List<Component>,
    @Required
    val unconfirmed: List<Component>,
    @Required
    val confirmed: List<Component>,
) : RendererFormat.Simple {
    override val id: String = "sell_button_usage"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = SingleSimpleTextMetaFactory(namespace, id)
}

@ConfigSerializable
internal data class SellButtonItemListRendererFormat(
    @Required
    override val namespace: String,
    @Required
    val withLevel: String,
    @Required
    val withoutLevel: String,
) : RendererFormat.Simple {
    override val id: String = "sell_button_item_list"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = SingleSimpleTextMetaFactory(namespace, id)
}

@ConfigSerializable
internal data class SellButtonTotalWorthRendererFormat(
    @Required
    override val namespace: String,
    @Required
    val totalWorth: List<String>,
) : RendererFormat.Simple {
    override val id: String = "sell_button_total_worth"
    override val index: DerivedIndex = createIndex()
    override val textMetaFactory: TextMetaFactory = SingleSimpleTextMetaFactory(namespace, id)
}