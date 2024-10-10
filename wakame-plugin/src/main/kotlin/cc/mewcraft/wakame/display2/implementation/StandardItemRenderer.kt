/**
 * 有关*标准*的渲染器实现.
 */
package cc.mewcraft.wakame.display2.implementation

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.attribute.composite.ConstantCompositeAttribute
import cc.mewcraft.wakame.attribute.composite.element
import cc.mewcraft.wakame.display2.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.item.components.*
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.SkillCore
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.packet.PacketNekoStack
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.skill.ConfiguredSkill
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import java.nio.file.Path

internal class StandardRendererLayout : AbstractRendererLayout() {
    override var staticIndexedTextList: List<IndexedText> = ArrayList() // TODO display2
    override var defaultIndexedTextList: List<IndexedText> = ArrayList() // TODO display2
}

internal class StandardRendererFormats : AbstractRendererFormats() {
    override fun <T : RendererFormat> get(id: String): T? {
        TODO("display2")
    }

    override fun <T : RendererFormat> set(id: String, format: T) {
        TODO("display2")
    }
}

internal class StandardContext

internal object StandardItemRenderer : AbstractItemRenderer<PacketNekoStack, StandardContext>(), KoinComponent {
    override val rendererLayout: AbstractRendererLayout = StandardRendererLayout()
    override val rendererFormats: AbstractRendererFormats = StandardRendererFormats()
    private val indexedTextFlatter: IndexedTextFlatter = IndexedTextFlatter(rendererLayout)

    override fun initialize(
        layoutPath: Path,
        formatPath: Path,
    ) {
        // TODO 读取配置文件, 初始化:
        //   rendererLayout, rendererFormats
        rendererLayout.initialize(layoutPath) // TODO display2
        rendererFormats.initialize(formatPath) // TODO display2
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
                    else -> StandardRenderingParts.CELLULAR_EMPTY.process(collector, null)
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

/**
 * 用于(反)序列化渲染器的配置文件.
 */
internal object StandardRendererConfigSerializer {

}


//////


//<editor-fold desc="RenderingPart">
internal object StandardRenderingParts {
    @JvmField
    val ATTACK_SPEED: StandardRenderingPart<ItemAttackSpeed, AttackSpeedRendererFormat> =
        configure("attack_speed") { data, format ->
            listOf(format.render(data))
        }

    @JvmField
    val CELLULAR_ATTRIBUTE: StandardRenderingPart<ConstantCompositeAttribute, CellularAttributeRendererFormat> =
        configure("cells/attributes") { data, format ->
            listOf(format.render(data))
        }

    @JvmField
    val CELLULAR_SKILL: StandardRenderingPart<ConfiguredSkill, CellularSkillRendererFormat> =
        configure("cells/skills") { data, format ->
            listOf(format.render(data))
        }

    @JvmField
    val CELLULAR_EMPTY: StandardRenderingPart<Nothing?, CellularEmptyRendererFormat> =
        configure("cells/empty") { _, format ->
            listOf(format.render())
        }

    @JvmField
    val CRATE: StandardRenderingPart<ItemCrate, AggregateValueRendererFormat> =
        configure("crate") { data, format ->
            listOf() // TODO display2
        }

    @JvmField
    val CUSTOM_NAME: StandardRenderingPart<CustomName, SingleValueRendererFormat> =
        configure("custom_name") { data, format ->
            listOf(format.render(Placeholder.component("value", data.rich)))
        }

    @JvmField
    val ELEMENTS: StandardRenderingPart<ItemElements, AggregateValueRendererFormat> =
        configure("elements") { data, format ->
            listOf(format.render(data.elements, Element::displayName))
        }

    @JvmField
    val ENCHANTMENTS: StandardRenderingPart<ItemEnchantments, EnchantmentRendererFormat> =
        configure("enchantments") { data, format ->
            listOf(format.render(data.enchantments))
        }

    @JvmField
    val FIRE_RESISTANT: StandardRenderingPart<FireResistant, SingleValueRendererFormat> =
        configure("fire_resistant") { _, format ->
            listOf(format.render())
        }

    @JvmField
    val FOOD: StandardRenderingPart<FoodProperties, ListValueRendererFormat> =
        configure("food") { data, format ->
            val resolver = TagResolver.resolver(
                Placeholder.component("nutrition", Component.text(data.nutrition)),
                Placeholder.component("saturation", Component.text(data.saturation)),
                Placeholder.component("eat_seconds", Component.text(data.eatSeconds)),
            )
            listOf(format.render(resolver))
        }

    @JvmField
    val ITEM_NAME: StandardRenderingPart<ItemName, SingleValueRendererFormat> =
        configure("item_name") { data, format ->
            listOf(format.render(Placeholder.component("value", data.rich)))
        }

    @JvmField
    val KIZAMIZ: StandardRenderingPart<ItemKizamiz, AggregateValueRendererFormat> = configure("kizamiz") { data, format ->
        listOf(format.render(data.kizamiz, Kizami::displayName))
    }

    @JvmField
    val LEVEL: StandardRenderingPart<ItemLevel, SingleValueRendererFormat> = configure("level") { data, format ->
        listOf(format.render(Placeholder.component("value", Component.text(data.level))))
    }

    @JvmField
    val LORE: StandardRenderingPart<ExtraLore, ExtraLoreRendererFormat> = configure("lore") { data, format ->
        listOf(format.render(data.lore))
    }

    @JvmField
    val PORTABLE_CORE: StandardRenderingPart<PortableCore, PortableCoreRendererFormat> = configure("portable_core") { data, format ->
        listOf(format.render(data)) // TODO display2 // 把核心的渲染逻辑分离出来, 不仅可以在这里 (PortableCore) 使用, 还可以在 ItemCells 使用
    }

    @JvmField
    val RARITY: StandardRenderingPart<ItemRarity, SingleValueRendererFormat> = configure("rarity") { data, format ->
        listOf(format.render(Placeholder.component("value", data.rarity.displayName)))
    }

    /**
     * @param id 用来定位配置文件中的节点
     * @param block 将数据渲染成文本的逻辑
     */
    private inline fun <T, reified F : RendererFormat> configure(id: String, block: IndexedDataRenderer<T, F>): StandardRenderingPart<T, F> {
        val format = StandardItemRenderer.rendererFormats.get<F>(id) ?: throw IllegalArgumentException("renderer format '$id' not found")
        val part = StandardRenderingPart(format, block)
        return part
    }

    /**
     * @see configure
     */
    private inline fun <T1, T2, reified F : RendererFormat> configure2(id: String, block: IndexedDataRenderer2<T1, T2, F>): StandardRenderingPart2<T1, T2, F> {
        val format = StandardItemRenderer.rendererFormats.get<F>(id) ?: throw IllegalArgumentException("renderer format '$id' not found")
        val part = StandardRenderingPart2(format, block)
        return part
    }

    /**
     * @see configure
     */
    private inline fun <T1, T2, T3, reified F : RendererFormat> configure3(id: String, block: IndexedDataRenderer3<T1, T2, T3, F>): StandardRenderingPart3<T1, T2, T3, F> {
        val format = StandardItemRenderer.rendererFormats.get<F>(id) ?: throw IllegalArgumentException("renderer format '$id' not found")
        val part = StandardRenderingPart3(format, block)
        return part
    }
}

/**
 * 聚合了渲染一种物品数据所需要的数据和逻辑.
 *
 * @param T 被渲染的数据类型
 * @param F 渲染格式的类型
 */
internal class StandardRenderingPart<T, F : RendererFormat>(
    override val format: F,
    override val renderer: IndexedDataRenderer<T, F>,
) : RenderingPart<T, F> {
    fun process(collector: MutableList<IndexedText>, data: T) {
        collector += renderer.render(data, format)
    }
}

internal class StandardRenderingPart2<T1, T2, F : RendererFormat>(
    override val format: F,
    override val renderer: IndexedDataRenderer2<T1, T2, F>,
) : RenderingPart2<T1, T2, F> {
    fun process(collector: MutableList<IndexedText>, data1: T1, data2: T2) {
        collector += renderer.render(data1, data2, format)
    }
}

internal class StandardRenderingPart3<T1, T2, T3, F : RendererFormat>(
    override val format: F,
    override val renderer: IndexedDataRenderer3<T1, T2, T3, F>,
) : RenderingPart3<T1, T2, T3, F> {
    fun process(collector: MutableList<IndexedText>, data1: T1, data2: T2, data3: T3) {
        collector += renderer.render(data1, data2, data3, format)
    }
}
//</editor-fold>


//////


//<editor-fold desc="RendererFormat">
@ConfigSerializable
internal data class AttackSpeedRendererFormat(
    @Setting override val namespace: String,
    @Setting override val tooltip: String,
) : SingleValueRendererFormat {
    override val id = "attack_speed"
    override val index: Key = createIndex()

    // TODO display2 mapping ENUM to String
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
    @Setting override val namespace: String,
) : RendererFormat.Dynamic<ConstantCompositeAttribute> {
    override fun computeIndex(source: ConstantCompositeAttribute): Key {
        val id = buildString {
            append(source.id)
            append('.')
            append(source.operation)
            source.element?.let {
                append('.')
                append(it.uniqueId)
            }
        }
        return Key.key(namespace, id)
    }

    fun render(attribute: ConstantCompositeAttribute): IndexedText {
        val facade = AttributeRegistry.FACADES[attribute.id]
        val tooltip = facade.createTooltipLore(attribute)
        return SimpleIndexedText(computeIndex(attribute), tooltip)
    }
}

@ConfigSerializable
internal data class CellularSkillRendererFormat(
    @Setting override val namespace: String,
) : RendererFormat.Dynamic<ConfiguredSkill>, KoinComponent {
    override fun computeIndex(source: ConfiguredSkill): Key {
        val sourceId = source.id
        return Key.key(namespace, sourceId.namespace() + "/" + sourceId.value())
    }

    fun render(skill: ConfiguredSkill): IndexedText {
        val instance = skill.instance
        val tooltip = instance.displays.tooltips.map(MM::deserialize)
        return SimpleIndexedText(computeIndex(skill), tooltip)
    }

    companion object Shared {
        private val MM = Injector.get<MiniMessage>()
    }
}

@ConfigSerializable
internal data class CellularEmptyRendererFormat(
    @Setting override val namespace: String,
    @Setting private val tooltip: List<Component>,
) : RendererFormat.Simple {
    override val id: String = "cells/empty"
    override val index: Key = createIndex()

    fun render(): IndexedText {
        return SimpleIndexedText(index, tooltip)
    }
}

// @ConfigSerializable
// internal data class CrateRendererFormat(
//     @Setting override val namespace: String,
//     @Setting override val tooltip: String,
// ) : SingleValueRendererFormat {
//     override val id = "crate"
//     override val index: Key = createIndex()
// }

// @ConfigSerializable
// internal data class CustomNameRendererFormat(
//     @Setting override val namespace: String,
//     @Setting override val tooltip: String,
// ) : SingleValueRendererFormat {
//     override val id = "custom_name"
//     override val index: Key = createIndex()
// }
//
// @ConfigSerializable
// internal data class ElementsRendererFormat(
//     @Setting override val namespace: String,
//     @Setting override val tooltip: AggregateValueRendererFormat.Tooltip,
// ) : AggregateValueRendererFormat {
//     override val id = "elements"
//     override val index: Key = createIndex()
// }
//
// @ConfigSerializable
// internal data class FireResistantRendererFormat(
//     @Setting override val namespace: String,
//     @Setting override val tooltip: String,
// ) : SingleValueRendererFormat {
//     override val id = "fire_resistant"
//     override val index: Key = createIndex()
// }
//
// @ConfigSerializable
// internal data class ItemNameRendererFormat(
//     @Setting override val namespace: String,
//     @Setting override val tooltip: String,
// ) : SingleValueRendererFormat {
//     override val id = "item_name"
//     override val index = createIndex()
// }
//
// @ConfigSerializable
// internal data class KizamizRendererFormat(
//     @Setting override val namespace: String,
//     @Setting override val tooltip: AggregateValueRendererFormat.Tooltip,
// ) : AggregateValueRendererFormat {
//     override val id = "kizamiz"
//     override val index = createIndex()
// }
//
// @ConfigSerializable
// internal data class LevelRendererFormat(
//     @Setting override val namespace: String,
//     @Setting override val tooltip: String,
// ) : SingleValueRendererFormat {
//     override val id = "level"
//     override val index = createIndex()
// }

@ConfigSerializable
internal data class PortableCoreRendererFormat(
    @Setting override val namespace: String,
) : RendererFormat.Dynamic<PortableCore> {
    private val unknownIndex = Key.key(namespace, "unknown")

    override fun computeIndex(source: PortableCore): Key {
        val core = source.wrapped as? AttributeCore ?: return unknownIndex
        val id = core.attribute.id
        return Key.key(namespace, id)
    }

    fun render(data: PortableCore): IndexedText {
        val core = data.wrapped as? AttributeCore ?: return SimpleIndexedText(unknownIndex, listOf())
        val facade = AttributeRegistry.FACADES[core.attribute.id]
        val tooltip = facade.createTooltipLore(core.attribute)
        return SimpleIndexedText(computeIndex(data), tooltip)
    }
}

// @ConfigSerializable
// internal data class RarityRendererFormat(
//     @Setting override val namespace: String,
//     @Setting override val tooltip: String,
// ) : SingleValueRendererFormat {
//     override val id = "rarity"
//     override val index: Key = createIndex()
// }
//</editor-fold>
