package cc.mewcraft.wakame.display2.implementation.crafting_station

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.*
import cc.mewcraft.wakame.display2.implementation.common.*
import cc.mewcraft.wakame.display2.implementation.standard.AttackSpeedRendererFormat
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.FoodProperties
import cc.mewcraft.wakame.item.components.ItemEnchantments
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.*
import cc.mewcraft.wakame.item.unsafeEdit
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.nio.file.Path


internal class CraftingStationRendererFormatRegistry(renderer: CraftingStationItemRenderer) : AbstractRendererFormatRegistry(renderer)

internal class CraftingStationRendererLayout(renderer: CraftingStationItemRenderer) : AbstractRendererLayout(renderer)

/**
 * @param pos 物品出现的位置
 * @param erase 是否移除萌芽标签
 */
internal data class CraftingStationContext(
    val pos: Pos,
    val erase: Boolean = false,
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
internal object CraftingStationItemRenderer : AbstractItemRenderer<NekoStack, CraftingStationContext>() {
    override val name: String = "crafting_station"
    override val formats = CraftingStationRendererFormatRegistry(this)
    override val layout = CraftingStationRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    @InitFun
    private fun init() {
        loadDataFromConfigs()
    }

    @ReloadFun
    private fun reload() {
        loadDataFromConfigs()
    }

    override fun initialize(formatPath: Path, layoutPath: Path) {
        CraftingStationRenderingHandlerRegistry.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: NekoStack, context: CraftingStationContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        val templates = item.templates
        templates.process(ItemTemplateTypes.ATTACK_SPEED) { data -> CraftingStationRenderingHandlerRegistry.ATTACK_SPEED.process(collector, data) }
        templates.process(ItemTemplateTypes.CELLS) { data -> CraftingStationRenderingHandlerRegistry.CELLS.process(collector, data) }
        templates.process(ItemTemplateTypes.CRATE) { data -> CraftingStationRenderingHandlerRegistry.CRATE.process(collector, data) }
        templates.process(ItemTemplateTypes.CUSTOM_NAME) { data -> CraftingStationRenderingHandlerRegistry.CUSTOM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.ELEMENTS) { data -> CraftingStationRenderingHandlerRegistry.ELEMENTS.process(collector, data) }
        templates.process(ItemTemplateTypes.DAMAGE_RESISTANT) { data -> CraftingStationRenderingHandlerRegistry.DAMAGE_RESISTANT.process(collector, data) }
        templates.process(ItemTemplateTypes.ITEM_NAME) { data -> CraftingStationRenderingHandlerRegistry.ITEM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.KIZAMIZ) { data -> CraftingStationRenderingHandlerRegistry.KIZAMIZ.process(collector, data) }
        templates.process(ItemTemplateTypes.LORE) { data -> CraftingStationRenderingHandlerRegistry.LORE.process(collector, data) }

        val components = item.components
        components.process(ItemComponentTypes.ENCHANTMENTS) { data -> CraftingStationRenderingHandlerRegistry.ENCHANTMENTS.process(collector, data) }
        components.process(ItemComponentTypes.FOOD) { data -> CraftingStationRenderingHandlerRegistry.FOOD.process(collector, data) }
        components.process(ItemComponentTypes.PORTABLE_CORE) { data -> CraftingStationRenderingHandlerRegistry.PORTABLE_CORE.process(collector, data) }
        components.process(ItemComponentTypes.STORED_ENCHANTMENTS) { data -> CraftingStationRenderingHandlerRegistry.ENCHANTMENTS.process(collector, data) }

        val itemLore = textAssembler.assemble(collector)

        if (context.erase) {
            item.erase()
        }

        item.unsafeEdit {
            lore = itemLore
            showAttributeModifiers(false)
            showEnchantments(false)
            showStoredEnchantments(false)
        }
    }
}

internal object CraftingStationRenderingHandlerRegistry : RenderingHandlerRegistry(CraftingStationItemRenderer) {
    @JvmField
    val ATTACK_SPEED: RenderingHandler<ItemAttackSpeed, AttackSpeedRendererFormat> = configure("attack_speed") { data, format ->
        format.render(data.level)
    }

    @JvmField
    val CELLS: RenderingHandler<ItemCells, SingleValueRendererFormat> = configure("cells") { data, format ->
        format.render(
            Placeholder.component("min", Component.text(data.minimumSlotAmount)),
            Placeholder.component("max", Component.text(data.maximumSlotAmount)),
        )
    }

    @JvmField
    val CRATE: RenderingHandler<ItemCrate, SingleValueRendererFormat> = configure("crate") { data, format ->
        format.render(
            Placeholder.component("id", Component.text(data.identity)),
            Placeholder.component("name", Component.text(data.identity)), // TODO display2 盲盒完成时再写这里
        )
    }

    @JvmField
    val CUSTOM_NAME: RenderingHandler<CustomName, SingleValueRendererFormat> = CommonRenderingHandlers.CUSTOM_NAME(this)

    @JvmField
    val ELEMENTS: RenderingHandler<ItemElements, AggregateValueRendererFormat> = configure("elements") { data, format ->
        val selector = data.selector
        val allPossibleElements = selector.allPossibleSamples
        val maximumElementAmount = selector.maximumSampleAmount
        val resolver = Placeholder.component("count", Component.text(maximumElementAmount))
        format.render(allPossibleElements, { it.value.displayName }, resolver)
    }

    @JvmField
    val ENCHANTMENTS: RenderingHandler<ItemEnchantments, FuzzyEnchantmentRendererFormat> = configure("enchantments") { data, format ->
        format.render(data.enchantments)
    }

    @JvmField
    val DAMAGE_RESISTANT: RenderingHandler<DamageResistant, SingleValueRendererFormat> = configure("damage_resistant") { _, format ->
        format.render()
    }

    @JvmField
    val FOOD: RenderingHandler<FoodProperties, ListValueRendererFormat> = configure("food") { data, format ->
        format.render(
            Placeholder.component("nutrition", Component.text(data.nutrition)),
            Placeholder.component("saturation", Component.text(data.saturation)),
        )
    }

    @JvmField
    val ITEM_NAME: RenderingHandler<ItemName, SingleValueRendererFormat> = CommonRenderingHandlers.ITEM_NAME(this)

    val KIZAMIZ: RenderingHandler<ItemKizamiz, AggregateValueRendererFormat> = configure("kizamiz") { data, format ->
        val selector = data.selector
        val allPossibleKizamiz = selector.allPossibleSamples
        val maximumKizamiAmount = selector.maximumSampleAmount
        val resolver = Placeholder.component("count", Component.text(maximumKizamiAmount))
        format.render(allPossibleKizamiz, { it.value.displayName }, resolver)
    }

    @JvmField
    val LORE: RenderingHandler<ExtraLore, ExtraLoreRendererFormat> = CommonRenderingHandlers.LORE(this)

    @JvmField
    val PORTABLE_CORE: RenderingHandler<PortableCore, FuzzyPortableCoreRendererFormat> = configure("portable_core") { data, format ->
        format.render(data)
    }
}
