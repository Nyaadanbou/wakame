package cc.mewcraft.wakame.display2.implementation.crafting_station

import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.implementation.AbstractItemRenderer
import cc.mewcraft.wakame.display2.implementation.AbstractRendererFormats
import cc.mewcraft.wakame.display2.implementation.AbstractRendererLayout
import cc.mewcraft.wakame.display2.implementation.RenderingPart
import cc.mewcraft.wakame.display2.implementation.RenderingParts
import cc.mewcraft.wakame.display2.implementation.common.AggregateValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.CommonRenderingParts
import cc.mewcraft.wakame.display2.implementation.common.ExtraLoreRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.ListValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.common.SingleValueRendererFormat
import cc.mewcraft.wakame.display2.implementation.standard.AttackSpeedRendererFormat
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.FoodProperties
import cc.mewcraft.wakame.item.components.ItemEnchantments
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.CustomName
import cc.mewcraft.wakame.item.templates.components.ExtraLore
import cc.mewcraft.wakame.item.templates.components.FireResistant
import cc.mewcraft.wakame.item.templates.components.ItemAttackSpeed
import cc.mewcraft.wakame.item.templates.components.ItemCells
import cc.mewcraft.wakame.item.templates.components.ItemCrate
import cc.mewcraft.wakame.item.templates.components.ItemElements
import cc.mewcraft.wakame.item.templates.components.ItemKizamiz
import cc.mewcraft.wakame.item.templates.components.ItemName
import cc.mewcraft.wakame.item.unsafeEdit
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.nio.file.Path


internal class CraftingStationRendererFormats(renderer: CraftingStationItemRenderer) : AbstractRendererFormats(renderer)

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
        OVERVIEW, // 合成站的主要菜单
        PREVIEW, // 合成站的预览菜单
        CHOICE, // station choice
        RESULT, // station result
    }
}

internal object CraftingStationItemRenderer : AbstractItemRenderer<NekoStack, CraftingStationContext>() {
    override val name: String = "crafting_station"
    override val formats = CraftingStationRendererFormats(this)
    override val layout = CraftingStationRendererLayout(this)
    private val textAssembler = TextAssembler(layout)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        CraftingStationRenderingParts.bootstrap()
        formats.initialize(formatPath)
        layout.initialize(layoutPath)
    }

    override fun render(item: NekoStack, context: CraftingStationContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        val collector = ReferenceOpenHashSet<IndexedText>()

        val templates = item.templates
        templates.process(ItemTemplateTypes.ATTACK_SPEED) { data -> CraftingStationRenderingParts.ATTACK_SPEED.process(collector, data) }
        templates.process(ItemTemplateTypes.CELLS) { data -> CraftingStationRenderingParts.CELLS.process(collector, data) }
        templates.process(ItemTemplateTypes.CRATE) { data -> CraftingStationRenderingParts.CRATE.process(collector, data) }
        templates.process(ItemTemplateTypes.CUSTOM_NAME) { data -> CraftingStationRenderingParts.CUSTOM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.ELEMENTS) { data -> CraftingStationRenderingParts.ELEMENTS.process(collector, data) }
        templates.process(ItemTemplateTypes.FIRE_RESISTANT) { data -> CraftingStationRenderingParts.FIRE_RESISTANT.process(collector, data) }
        templates.process(ItemTemplateTypes.ITEM_NAME) { data -> CraftingStationRenderingParts.ITEM_NAME.process(collector, data) }
        templates.process(ItemTemplateTypes.KIZAMIZ) { data -> CraftingStationRenderingParts.KIZAMIZ.process(collector, data) }
        templates.process(ItemTemplateTypes.LORE) { data -> CraftingStationRenderingParts.LORE.process(collector, data) }

        val components = item.components
        components.process(ItemComponentTypes.ENCHANTMENTS) { data -> CraftingStationRenderingParts.ENCHANTMENTS.process(collector, data) }
        components.process(ItemComponentTypes.FOOD) { data -> CraftingStationRenderingParts.FOOD.process(collector, data) }
        components.process(ItemComponentTypes.PORTABLE_CORE) { data -> CraftingStationRenderingParts.PORTABLE_CORE.process(collector, data) }
        components.process(ItemComponentTypes.STORED_ENCHANTMENTS) { data -> CraftingStationRenderingParts.ENCHANTMENTS.process(collector, data) }

        val itemLore = textAssembler.assemble(collector)
        val itemCustomModelData = ItemModelDataLookup[item.id, item.variant]

        if (context.erase) {
            item.erase()
        }

        item.unsafeEdit {
            lore = itemLore
            customModelData = itemCustomModelData
            showAttributeModifiers(false)
            showEnchantments(false)
            showStoredEnchantments(false)
        }
    }
}

internal object CraftingStationRenderingParts : RenderingParts(CraftingStationItemRenderer) {
    @JvmField
    val ATTACK_SPEED: RenderingPart<ItemAttackSpeed, AttackSpeedRendererFormat> = configure("attack_speed") { data, format ->
        format.render(data.level)
    }

    @JvmField
    val CELLS: RenderingPart<ItemCells, SingleValueRendererFormat> = configure("cells") { data, format ->
        format.render(
            Placeholder.component("min", Component.text(data.minimumSlotAmount)),
            Placeholder.component("max", Component.text(data.maximumSlotAmount)),
        )
    }

    @JvmField
    val CRATE: RenderingPart<ItemCrate, SingleValueRendererFormat> = configure("crate") { data, format ->
        format.render(
            Placeholder.component("id", Component.text(data.identity)),
            Placeholder.component("name", Component.text(data.identity)), // TODO display2 盲盒完成时再写这里
        )
    }

    @JvmField
    val CUSTOM_NAME: RenderingPart<CustomName, SingleValueRendererFormat> = CommonRenderingParts.CUSTOM_NAME(this)

    @JvmField
    val ELEMENTS: RenderingPart<ItemElements, AggregateValueRendererFormat> = configure("elements") { data, format ->
        val collection = data.possibleElements
        val resolver = Placeholder.component("count", Component.text(collection.size))
        format.render(collection, Element::displayName, resolver)
    }

    @JvmField
    val ENCHANTMENTS: RenderingPart<ItemEnchantments, FuzzyEnchantmentRendererFormat> = configure("enchantments") { data, format ->
        format.render(data.enchantments)
    }

    @JvmField
    val FIRE_RESISTANT: RenderingPart<FireResistant, SingleValueRendererFormat> = configure("fire_resistant") { _, format ->
        format.render()
    }

    @JvmField
    val FOOD: RenderingPart<FoodProperties, ListValueRendererFormat> = configure("food") { data, format ->
        format.render(
            Placeholder.component("nutrition", Component.text(data.nutrition)),
            Placeholder.component("saturation", Component.text(data.saturation)),
            Placeholder.component("eat_seconds", Component.text(data.eatSeconds)),
        )
    }

    @JvmField
    val ITEM_NAME: RenderingPart<ItemName, SingleValueRendererFormat> = CommonRenderingParts.ITEM_NAME(this)

    val KIZAMIZ: RenderingPart<ItemKizamiz, AggregateValueRendererFormat> = configure("kizamiz") { data, format ->
        val collection = data.possibleKizamiz
        val resolver = Placeholder.component("count", Component.text(collection.size))
        format.render(collection, Kizami::displayName, resolver)
    }

    @JvmField
    val LORE: RenderingPart<ExtraLore, ExtraLoreRendererFormat> = CommonRenderingParts.LORE(this)

    @JvmField
    val PORTABLE_CORE: RenderingPart<PortableCore, FuzzyPortableCoreRendererFormat> = configure("portable_core") { data, format ->
        format.render(data)
    }
}
