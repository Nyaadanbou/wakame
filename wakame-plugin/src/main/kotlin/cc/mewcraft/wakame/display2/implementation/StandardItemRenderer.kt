/**
 * 有关*标准*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.argument.StringArgumentQueue
import cc.mewcraft.wakame.attribute.AttributeModifier.*
import cc.mewcraft.wakame.attribute.composite.*
import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.item.components.*
import cc.mewcraft.wakame.item.components.cells.*
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.packet.PacketNekoStack
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.util.StringCombiner
import cc.mewcraft.wakame.util.yamlConfig
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.koin.core.component.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.reflect.typeOf

internal class StandardRendererLayout : AbstractRendererLayout() {
    override var staticIndexedTexts: List<IndexedText> = ArrayList() // TODO display2
    override var defaultIndexedTexts: List<IndexedText> = ArrayList() // TODO display2
}

internal class StandardRendererFormats : AbstractRendererFormats()
internal class StandardContext

internal object StandardItemRenderer : AbstractItemRenderer<PacketNekoStack, StandardContext>() {
    override val rendererLayout: AbstractRendererLayout = StandardRendererLayout()
    override val rendererFormats: AbstractRendererFormats = StandardRendererFormats()
    private val indexedTextFlatter: IndexedTextFlatter = IndexedTextFlatter(rendererLayout)

    override fun initialize(
        layoutPath: Path,
        formatPath: Path,
    ) {
        StandardRenderingParts.bootstrap()
        rendererLayout.initialize(layoutPath) // TODO display2
        rendererFormats.initialize(formatPath)
    }

    override fun render(item: PacketNekoStack, context: StandardContext?) {
        requireNotNull(context) { "context" }

        val components = item.components
        val collector = ObjectArrayList<IndexedText>()

        components.process(ItemComponentTypes.ATTACK_SPEED) { data -> StandardRenderingParts.ATTACK_SPEED.process(collector, data) }
        components.process(ItemComponentTypes.CELLS) { data ->
            for ((_, cell) in data) {
                when (
                    val core = cell.getCore()
                ) {
                    is AttributeCore -> StandardRenderingParts.CELLULAR_ATTRIBUTE.process(collector, core.attribute)
                    is SkillCore -> StandardRenderingParts.CELLULAR_SKILL.process(collector, core.skill)
                    is EmptyCore -> StandardRenderingParts.CELLULAR_EMPTY.process(collector, null)
                }
            }
        }
        components.process(ItemComponentTypes.CRATE) { data -> StandardRenderingParts.CRATE.process(collector, data) }
        components.process(ItemComponentTypes.CUSTOM_NAME) { data -> StandardRenderingParts.CUSTOM_NAME.process(collector, data) }
        components.process(ItemComponentTypes.ELEMENTS) { data -> StandardRenderingParts.ELEMENTS.process(collector, data) }
        components.process(ItemComponentTypes.ENCHANTMENTS) { data -> StandardRenderingParts.ENCHANTMENTS.process(collector, data) }
        components.process(ItemComponentTypes.FIRE_RESISTANT) { data -> StandardRenderingParts.FIRE_RESISTANT.process(collector, data) }
        components.process(ItemComponentTypes.FOOD) { data -> StandardRenderingParts.FOOD.process(collector, data) }
        components.process(ItemComponentTypes.ITEM_NAME) { data -> StandardRenderingParts.ITEM_NAME.process(collector, data) }
        components.process(ItemComponentTypes.KIZAMIZ) { data -> StandardRenderingParts.KIZAMIZ.process(collector, data) }
        components.process(ItemComponentTypes.LEVEL) { data -> StandardRenderingParts.LEVEL.process(collector, data) }
        components.process(ItemComponentTypes.LORE) { data -> StandardRenderingParts.LORE.process(collector, data) }
        components.process(ItemComponentTypes.PORTABLE_CORE) { data -> StandardRenderingParts.PORTABLE_CORE.process(collector, data) }
        components.process(ItemComponentTypes.RARITY) { data -> StandardRenderingParts.RARITY.process(collector, data) }

        val lore = indexedTextFlatter.flatten(collector)
        val cmd = ItemModelDataLookup[item.id, item.variant]

        // 修改物品(原地)
        item.lore(lore)
        item.customModelData(cmd)
    }

    private inline fun <T> ItemComponentMap.process(type: ItemComponentType<T>, block: (T) -> Unit) {
        get(type)?.apply(block)
    }
}


//////


internal object StandardRenderingParts {
    @JvmField
    val ATTACK_SPEED: SimpleRenderingPart<ItemAttackSpeed, AttackSpeedRendererFormat> = configure("attack_speed") { data, format ->
        format.render(data)
    }

    @JvmField
    val CELLULAR_ATTRIBUTE: SimpleRenderingPart<ConstantCompositeAttribute, CellularAttributeRendererFormat> = configure("cells/attributes") { data, format ->
        format.render(data)
    }

    @JvmField
    val CELLULAR_SKILL: SimpleRenderingPart<ConfiguredSkill, CellularSkillRendererFormat> = configure("cells/skills") { data, format ->
        format.render(data)
    }

    @JvmField
    val CELLULAR_EMPTY: SimpleRenderingPart<Nothing?, CellularEmptyRendererFormat> = configure("cells/empty") { _, format ->
        format.render()
    }

    @JvmField
    val CRATE: SimpleRenderingPart<ItemCrate, SingleValueRendererFormat> = configure("crate") { data, format ->
        val resolver = TagResolver.resolver(
            Placeholder.component("id", Component.text(data.identity)),
            Placeholder.component("name", Component.text(data.identity)), // TODO display2 盲盒完成时再写这里
        )
        format.render(resolver)
    }

    @JvmField
    val CUSTOM_NAME: SimpleRenderingPart<CustomName, SingleValueRendererFormat> = configure("custom_name") { data, format ->
        format.render(Placeholder.component("value", data.rich))
    }

    @JvmField
    val ELEMENTS: SimpleRenderingPart<ItemElements, AggregateValueRendererFormat> = configure("elements") { data, format ->
        format.render(data.elements, Element::displayName)
    }

    @JvmField
    val ENCHANTMENTS: SimpleRenderingPart<ItemEnchantments, EnchantmentRendererFormat> = configure("enchantments") { data, format ->
        format.render(data.enchantments)
    }

    @JvmField
    val FIRE_RESISTANT: SimpleRenderingPart<FireResistant, SingleValueRendererFormat> = configure("fire_resistant") { _, format ->
        format.render()
    }

    @JvmField
    val FOOD: SimpleRenderingPart<FoodProperties, ListValueRendererFormat> = configure("food") { data, format ->
        val resolver = TagResolver.resolver(
            Placeholder.component("nutrition", Component.text(data.nutrition)),
            Placeholder.component("saturation", Component.text(data.saturation)),
            Placeholder.component("eat_seconds", Component.text(data.eatSeconds)),
        )
        format.render(resolver)
    }

    @JvmField
    val ITEM_NAME: SimpleRenderingPart<ItemName, SingleValueRendererFormat> = configure("item_name") { data, format ->
        format.render(Placeholder.component("value", data.rich))
    }

    @JvmField
    val KIZAMIZ: SimpleRenderingPart<ItemKizamiz, AggregateValueRendererFormat> = configure("kizamiz") { data, format ->
        format.render(data.kizamiz, Kizami::displayName)
    }

    @JvmField
    val LEVEL: SimpleRenderingPart<ItemLevel, SingleValueRendererFormat> = configure("level") { data, format ->
        format.render(Placeholder.component("value", Component.text(data.level)))
    }

    @JvmField
    val LORE: SimpleRenderingPart<ExtraLore, ExtraLoreRendererFormat> = configure("lore") { data, format ->
        format.render(data.lore)
    }

    @JvmField
    val PORTABLE_CORE: SimpleRenderingPart<PortableCore, PortableCoreRendererFormat> = configure("portable_core") { data, format ->
        format.render(data)
    }

    @JvmField
    val RARITY: SimpleRenderingPart<ItemRarity, SingleValueRendererFormat> = configure("rarity") { data, format ->
        format.render(Placeholder.component("value", data.rarity.displayName))
    }

    fun bootstrap() = Unit // to explicitly initialize static block

    /**
     * @param id 用来定位配置文件中的节点
     * @param block 将数据渲染成文本的逻辑
     */
    private inline fun <T, reified F : RendererFormat> configure(id: String, block: IndexedDataRenderer<T, F>): SimpleRenderingPart<T, F> =
        SimpleRenderingPart(provideFormat<F>(id), block)

    private inline fun <T1, T2, reified F : RendererFormat> configure2(id: String, block: IndexedDataRenderer2<T1, T2, F>): SimpleRenderingPart2<T1, T2, F> =
        SimpleRenderingPart2(provideFormat<F>(id), block)

    private inline fun <T1, T2, T3, reified F : RendererFormat> configure3(id: String, block: IndexedDataRenderer3<T1, T2, T3, F>): SimpleRenderingPart3<T1, T2, T3, F> =
        SimpleRenderingPart3(provideFormat<F>(id), block)

    private inline fun <reified F : RendererFormat> provideFormat(id: String): Provider<F> {
        try {
            val added = StandardItemRenderer.rendererFormats.register(id, typeOf<F>())
            val format = StandardItemRenderer.rendererFormats.get0<F>(id)
            return format
        } catch (e: Exception) {
            throw ExceptionInInitializerError(e)
        }
    }
}


//////


//<editor-fold desc="RendererFormat">
@ConfigSerializable
internal data class AttackSpeedRendererFormat(
    @Setting @Required
    override val namespace: String,
    @Setting
    private val tooltip: String = "Attack Speed: <value>",
) : RendererFormat.Simple {
    override val id = "attack_speed"
    override val index: Key = createIndex()

    // TODO display2 mapping Enum to String
    fun render(data: ItemAttackSpeed): IndexedText {
        val resolver = Placeholder.component("value", Component.text(data.level.name))
        return SimpleIndexedText(index, listOf(MM.deserialize(tooltip, resolver)))
    }

    companion object Shared {
        private val MM = Injector.get<MiniMessage>()
    }
}

@ConfigSerializable
internal data class CellularAttributeRendererFormat(
    @Setting @Required
    override val namespace: String,
) : RendererFormat.Dynamic<ConstantCompositeAttribute> {
    override fun computeIndex(data: ConstantCompositeAttribute): Key {
        val indexId = buildString {
            append(data.id)
            append('.')
            append(data.operation)
            data.element?.let {
                append('.')
                append(it.uniqueId)
            }
        }
        return Key.key(namespace, indexId)
    }

    fun render(data: ConstantCompositeAttribute): IndexedText {
        val facade = AttributeRegistry.FACADES[data.id]
        val tooltip = facade.createTooltipLore(data)
        return SimpleIndexedText(computeIndex(data), tooltip)
    }
}

@ConfigSerializable
internal data class CellularSkillRendererFormat(
    @Setting @Required
    override val namespace: String,
) : RendererFormat.Dynamic<ConfiguredSkill> {
    override fun computeIndex(data: ConfiguredSkill): Key {
        val dataId = data.id
        val indexId = dataId.namespace() + "/" + dataId.value()
        return Key.key(namespace, indexId)
    }

    fun render(data: ConfiguredSkill): IndexedText {
        val instance = data.instance
        val tooltip = instance.displays.tooltips.map(MM::deserialize)
        return SimpleIndexedText(computeIndex(data), tooltip)
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
    private val tooltip: List<Component> = listOf(
        Component.text("Empty Slot")
    ),
) : RendererFormat.Simple {
    override val id: String = "cells/empty"
    override val index: Key = createIndex()

    fun render(): IndexedText {
        return SimpleIndexedText(index, tooltip)
    }
}

@ConfigSerializable
internal data class PortableCoreRendererFormat(
    @Setting @Required
    override val namespace: String,
) : RendererFormat.Dynamic<PortableCore> {
    private val unknownIndex = Key.key(namespace, "unknown")

    override fun computeIndex(data: PortableCore): Key {
        throw UnsupportedOperationException() // 直接在 render 函数中处理
    }

    fun render(data: PortableCore): IndexedText {
        val core = data.wrapped as? AttributeCore ?: return SimpleIndexedText(unknownIndex, listOf())
        val index = Key.key(namespace, core.attribute.id)
        val facade = AttributeRegistry.FACADES[core.attribute.id]
        val tooltip = facade.createTooltipLore(core.attribute)
        return SimpleIndexedText(index, tooltip)
    }
}
//</editor-fold>
