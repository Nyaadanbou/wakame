package cc.mewcraft.wakame.display2.implementation.crafting_station

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.AbstractItemRenderer
import cc.mewcraft.wakame.display2.implementation.AbstractRendererFormatRegistry
import cc.mewcraft.wakame.display2.implementation.AbstractRendererLayout
import cc.mewcraft.wakame.display2.implementation.RenderingHandler
import cc.mewcraft.wakame.display2.implementation.RenderingHandlerRegistry
import cc.mewcraft.wakame.display2.implementation.common.AggregateValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.CommonRenderingHandlers
import cc.mewcraft.wakame.display2.implementation.common.ExtraLoreRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.ListValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.SingleValueRendererFormat
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaTypes
import cc.mewcraft.wakame.item2.config.datagen.impl.MetaCoreContainer
import cc.mewcraft.wakame.item2.config.datagen.impl.MetaCustomName
import cc.mewcraft.wakame.item2.config.datagen.impl.MetaItemName
import cc.mewcraft.wakame.item2.config.datagen.impl.MetaKizami
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.config.property.impl.ExtraLore
import cc.mewcraft.wakame.item2.isNetworkRewrite
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.util.item.fastLore
import cc.mewcraft.wakame.util.item.hideAttributeModifiers
import cc.mewcraft.wakame.util.item.hideEnchantments
import cc.mewcraft.wakame.util.item.hideStoredEnchantments
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.FoodProperties
import io.papermc.paper.datacomponent.item.ItemEnchantments
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.inventory.ItemStack
import java.nio.file.Path

internal class CraftingStationRendererFormatRegistry(renderer: CraftingStationItemRenderer) : AbstractRendererFormatRegistry(renderer)

internal class CraftingStationRendererLayout(renderer: CraftingStationItemRenderer) : AbstractRendererLayout(renderer)

/**
 * @param pos 物品出现的位置
 */
internal data class CraftingStationContext(
    val pos: Pos,
) {
    enum class Pos {
        CHOICE, // station choice
        RESULT, // station result
    }
}

@Init(
    stage = InitStage.POST_WORLD
)
@Reload
internal object CraftingStationItemRenderer : AbstractItemRenderer<CraftingStationContext>() {
    override val name: String = "crafting_station"
    override val formats = CraftingStationRendererFormatRegistry(this)
    override val layout = CraftingStationRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    @InitFun
    fun init() {
        loadDataFromConfigs()
    }

    @ReloadFun
    fun reload() {
        loadDataFromConfigs()
    }

    override fun initialize(formatPath: Path, layoutPath: Path) {
        CraftingStationRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: ItemStack, context: CraftingStationContext?) {
        requireNotNull(context) { "context" }

        item.isNetworkRewrite = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        item.process(ItemMetaTypes.CORE_CONTAINER) { data -> CraftingStationRenderingHandlerRegistry.CORE_CONTAINER.process(collector, data) }
        item.process(ItemMetaTypes.CUSTOM_NAME) { data -> CraftingStationRenderingHandlerRegistry.CUSTOM_NAME.process(collector, data) }
        item.process(ItemMetaTypes.ITEM_NAME) { data -> CraftingStationRenderingHandlerRegistry.ITEM_NAME.process(collector, data) }
        item.process(ItemMetaTypes.KIZAMI) { data -> CraftingStationRenderingHandlerRegistry.KIZAMIZ.process(collector, data) }

        item.process(ItemPropertyTypes.EXTRA_LORE) { data -> CraftingStationRenderingHandlerRegistry.LORE.process(collector, data) }

        item.process(DataComponentTypes.ENCHANTMENTS) { data -> CraftingStationRenderingHandlerRegistry.ENCHANTMENTS.process(collector, data) }
        item.process(DataComponentTypes.FOOD) { data -> CraftingStationRenderingHandlerRegistry.FOOD.process(collector, data) }

        val lore = textAssembler.assemble(collector)
        item.fastLore(lore)

        item.hideAttributeModifiers()
        item.hideEnchantments()
        item.hideStoredEnchantments()
    }
}

internal object CraftingStationRenderingHandlerRegistry : RenderingHandlerRegistry(CraftingStationItemRenderer) {
    @JvmField
    val CORE_CONTAINER: RenderingHandler<MetaCoreContainer, SingleValueRendererFormat> = configure("core_container") { data, format ->
        when (data) {
            is MetaCoreContainer.Static -> {
                format.render(Placeholder.parsed("count", data.entry.size.toString()))
            }
            is MetaCoreContainer.Dynamic -> {
                // TODO #373: 实现动态生成
                IndexedText.NOP
            }
        }
    }

    @JvmField
    val CUSTOM_NAME: RenderingHandler<MetaCustomName, SingleValueRendererFormat> = CommonRenderingHandlers.CUSTOM_NAME(this)

    @JvmField
    val ENCHANTMENTS: RenderingHandler<ItemEnchantments, FuzzyEnchantmentRendererFormat> = configure("enchantments") { data, format ->
        format.render(data.enchantments())
    }

    @JvmField
    val FOOD: RenderingHandler<FoodProperties, ListValueRendererFormat> = configure("food") { data, format ->
        format.render(
            Placeholder.component("nutrition", Component.text(data.nutrition())),
            Placeholder.component("saturation", Component.text(data.saturation())),
        )
    }

    @JvmField
    val ITEM_NAME: RenderingHandler<MetaItemName, SingleValueRendererFormat> = CommonRenderingHandlers.ITEM_NAME(this)

    val KIZAMIZ: RenderingHandler<MetaKizami, AggregateValueRendererFormat> = configure("kizami") { data, format ->
        when (data) {
            is MetaKizami.Static -> {
                format.render(data.entries, { it.unwrap().displayName })
            }

            is MetaKizami.Dynamic -> {
                // TODO #373: 实现动态生成
                IndexedText.NOP
            }
        }
    }

    @JvmField
    val LORE: RenderingHandler<ExtraLore, ExtraLoreRendererFormat> = CommonRenderingHandlers.LORE(this)
}
