/**
 * 有关*合成站*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.CustomName
import cc.mewcraft.wakame.item.components.FoodProperties
import cc.mewcraft.wakame.item.components.ItemEnchantments
import cc.mewcraft.wakame.item.components.ItemName
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.ExtraLore
import cc.mewcraft.wakame.item.templates.components.FireResistant
import cc.mewcraft.wakame.item.templates.components.ItemAttackSpeed
import cc.mewcraft.wakame.item.templates.components.ItemCells
import cc.mewcraft.wakame.item.templates.components.ItemCrate
import cc.mewcraft.wakame.item.templates.components.ItemElements
import cc.mewcraft.wakame.item.templates.components.ItemKizamiz
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.*
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.enchantments.Enchantment
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.nio.file.Path

internal class CraftingStationRendererFormats(renderer: CraftingStationItemRenderer) : AbstractRendererFormats(renderer)

internal class CraftingStationRendererLayout(renderer: CraftingStationItemRenderer) : AbstractRendererLayout(renderer)

/**
 * @param pos 物品出现的位置
 */
internal data class CraftingStationContext(
    val pos: Pos,
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
    override val rendererFormats = CraftingStationRendererFormats(this)
    override val rendererLayout = CraftingStationRendererLayout(this)
    private val indexedTextListTransformer = IndexedTextListTransformer(rendererLayout)

    override fun initialize(formatPath: Path, layoutPath: Path) {
        CraftingStationRenderingParts.bootstrap()
        rendererFormats.initialize(formatPath)
        rendererLayout.initialize(layoutPath)
    }

    override fun render(item: NekoStack, context: CraftingStationContext?) {
        requireNotNull(context) { "context" }

        item.isClientSide = false

        val collector = ObjectArrayList<IndexedText>()

        val templates = item.templates
        templates.process(ItemTemplateTypes.ATTACK_SPEED) { data -> CraftingStationRenderingParts.ATTACK_SPEED.process(collector, data) }
        templates.process(ItemTemplateTypes.CELLS) { data -> CraftingStationRenderingParts.CELLS.process(collector, data) }
        templates.process(ItemTemplateTypes.CRATE) { data -> CraftingStationRenderingParts.CRATE.process(collector, data) }
        templates.process(ItemTemplateTypes.ELEMENTS) { data -> CraftingStationRenderingParts.ELEMENTS.process(collector, data) }
        templates.process(ItemTemplateTypes.FIRE_RESISTANT) { data -> CraftingStationRenderingParts.FIRE_RESISTANT.process(collector, data) }
        templates.process(ItemTemplateTypes.KIZAMIZ) { data -> CraftingStationRenderingParts.KIZAMIZ.process(collector, data) }
        templates.process(ItemTemplateTypes.LORE) { data -> CraftingStationRenderingParts.LORE.process(collector, data) }

        val components = item.components
        components.process(ItemComponentTypes.CUSTOM_NAME) { data -> CraftingStationRenderingParts.CUSTOM_NAME.process(collector, data) }
        components.process(ItemComponentTypes.ENCHANTMENTS) { data -> CraftingStationRenderingParts.ENCHANTMENTS.process(collector, data) }
        components.process(ItemComponentTypes.FOOD) { data -> CraftingStationRenderingParts.FOOD.process(collector, data) }
        components.process(ItemComponentTypes.ITEM_NAME) { data -> CraftingStationRenderingParts.ITEM_NAME.process(collector, data) }
        components.process(ItemComponentTypes.PORTABLE_CORE) { data -> CraftingStationRenderingParts.PORTABLE_CORE.process(collector, data) }
        components.process(ItemComponentTypes.STORED_ENCHANTMENTS) { data -> CraftingStationRenderingParts.ENCHANTMENTS.process(collector, data) }

        val minecraftLore = indexedTextListTransformer.flatten(collector)
        val minecraftCmd = ItemModelDataLookup[item.id, item.variant]

        // item.erase()

        val handle = item.unsafe.handle
        handle.backingLore = minecraftLore
        handle.backingCustomModelData = minecraftCmd
        handle.showAttributeModifiers(false)
        // handle.showCanBreak(false)
        // handle.showCanPlaceOn(false)
        // handle.showDyedColor(false)
        handle.showEnchantments(false)
        // handle.showJukeboxPlayable(false)
        handle.showStoredEnchantments(false)
        // handle.showTrim(false)
        // handle.showUnbreakable(false)
    }
}


//////


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
    val CUSTOM_NAME: RenderingPart<CustomName, SingleValueRendererFormat> = configure("custom_name") { data, format ->
        format.render(Placeholder.component("value", data.rich))
    }

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
    val ITEM_NAME: RenderingPart<ItemName, SingleValueRendererFormat> = configure("item_name") { data, format ->
        format.render(Placeholder.component("value", data.rich))
    }

    val KIZAMIZ: RenderingPart<ItemKizamiz, AggregateValueRendererFormat> = configure("kizamiz") { data, format ->
        val collection = data.possibleKizamiz
        val resolver = Placeholder.component("count", Component.text(collection.size))
        format.render(collection, Kizami::displayName, resolver)
    }

    @JvmField
    val LORE: RenderingPart<ExtraLore, ExtraLoreRendererFormat> = configure("lore") { data, format ->
        format.render(data.lore)
    }

    @JvmField
    val PORTABLE_CORE: RenderingPart<PortableCore, FuzzyPortableCoreRendererFormat> = configure("portable_core") { data, format ->
        format.render(data)
    }
}


//////


//<editor-fold desc="RendererFormat">
@ConfigSerializable
internal data class FuzzyEnchantmentRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting
    private val tooltip: String = "<name> <level>",
) : RendererFormat.Simple {
    override val id = "enchantments"
    override val index = Key.key(namespace, id)

    override fun createTextMetaFactory(): TextMetaFactory {
        return SingleSimpleTextMetaFactory(namespace, id)
    }

    /**
     * @param data 魔咒和等级的映射
     */
    fun render(data: Map<Enchantment, Int>): IndexedText {
        val tooltip = data.map { (enchantment, level) ->
            MM.deserialize(
                tooltip,
                Placeholder.component("name", enchantment.description()),
                Placeholder.component("level", Component.text(level)),
            )
        }
        return SimpleIndexedText(index, tooltip)
    }

    companion object {
        private val MM = Injector.get<MiniMessage>()
    }
}

@ConfigSerializable
internal data class FuzzyPortableCoreRendererFormat(
    @Setting @Required
    override val namespace: String,
) : RendererFormat.Dynamic<PortableCore> {
    private val unknownIndex = Key.key(namespace, "unknown")

    fun render(data: PortableCore): IndexedText {
        val core = data.wrapped as? AttributeCore ?: return SimpleIndexedText(unknownIndex, listOf())
        val index = Key.key(namespace, core.attribute.id)
        val tooltip = AttributeRegistry.FACADES[core.attribute.id].createTooltipLore(core.attribute)
        return SimpleIndexedText(index, tooltip)
    }

    override fun computeIndex(data: PortableCore): Key {
        throw UnsupportedOperationException() // 直接在 render(...) 函数中处理
    }

    override fun createTextMetaFactory(): TextMetaFactory {
        return FuzzyPortableCoreTextMetaFactory(namespace)
    }
}
//</editor-fold>


//////


//<editor-fold desc="TextMeta">
internal data class FuzzyPortableCoreTextMetaFactory(
    override val namespace: String,
) : TextMetaFactory {
    override fun test(sourceIndex: SourceIndex): Boolean {
        return sourceIndex.namespace() == namespace && sourceIndex.value() == SOURCE_INDEX_VALUE
    }

    override fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta {
        return SingleSimpleTextMeta(sourceIndex, sourceOrdinal, defaultText)
    }

    companion object Shared {
        const val SOURCE_INDEX_VALUE = "portable_core"
    }
}
//</editor-fold>