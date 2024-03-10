package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.facade.*
import cc.mewcraft.wakame.display.ItemMetaStylizer.ChildStylizer
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.binary.core.BinaryAbilityCore
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.binary.core.isEmpty
import cc.mewcraft.wakame.item.binary.meta.BinaryItemMeta
import cc.mewcraft.wakame.item.binary.meta.RarityMeta
import cc.mewcraft.wakame.item.binary.meta.get
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.reloadable
import cc.mewcraft.wakame.util.getOrThrow
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.TagPattern
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.*
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.reflect.KClass
import cc.mewcraft.wakame.item.binary.meta.DisplayLoreMeta as BDisplayLoreMeta
import cc.mewcraft.wakame.item.binary.meta.DisplayNameMeta as BDisplayNameMeta
import cc.mewcraft.wakame.item.binary.meta.DurabilityMeta as BDurabilityMeta
import cc.mewcraft.wakame.item.binary.meta.ElementMeta as BElementMeta
import cc.mewcraft.wakame.item.binary.meta.KizamiMeta as BKizamiMeta
import cc.mewcraft.wakame.item.binary.meta.LevelMeta as BLevelMeta
import cc.mewcraft.wakame.item.binary.meta.RarityMeta as BRarityMeta
import cc.mewcraft.wakame.item.binary.meta.SkinMeta as BSkinMeta
import cc.mewcraft.wakame.item.binary.meta.SkinOwnerMeta as BSkinOwnerMeta

// TODO 所有的 stylizer 应该尽可能的实现缓存机制

internal class TextStylizerImpl(
    /* sub stylizers */
    private val itemMetaStylizer: ItemMetaStylizer,
    private val abilityStylizer: AbilityStylizer,
    private val attributeStylizer: AttributeStylizer,

    /* full key suppliers */
    private val itemMetaKeySupplier: ItemMetaKeySupplier,
    private val abilityKeySupplier: AbilityKeySupplier,
    private val attributeKeySupplier: AttributeKeySupplier,
) : TextStylizer {
    override fun stylizeName(item: NekoItemStack): Component {
        return itemMetaStylizer.stylizeName(item)
    }

    override fun stylizeLore(item: NekoItemStack): Collection<LoreLine> {
        val ret = ObjectArrayList<LoreLine>(16)

        // for each meta in the item
        for ((itemMetaKClass, itemMeta) in item.metadata.map) {
            // Somehow the `::class` on the same type can return different KClass references.
            // We have to use the `.java` to compare references. Kotlin sucks this time :(
            if (itemMetaKClass.java === BDisplayNameMeta::class.java) {
                continue // displayName has been rendered separately
            }

            val key = itemMetaKeySupplier.get(itemMeta)
            if (key === SKIP_RENDERING) continue
            val lines = itemMetaStylizer.getChildStylizerBy(itemMetaKClass).stylize(itemMeta)
            val wrapped = ItemMetaLineFactory.get(key, lines)
            ret += wrapped
        }

        // for each cell in the item
        for (cell in item.cells.map.values) {
            val core = cell.binaryCore
            if (core.isEmpty) {
                // it's an empty core - add the pre-defined placeholder lines

                ret += AttributeLineFactory.empty() // TODO 词条栏系统应该限制可替换的核心类型
            } else when (core) {
                // it's a non-empty core - dynamically generate the lines

                is BinaryAbilityCore -> {
                    val key = abilityKeySupplier.get(core)
                    if (key === SKIP_RENDERING) continue
                    val lines = abilityStylizer.stylize(core)
                    ret += AbilityLineFactory.get(key, lines)
                }

                is BinaryAttributeCore -> {
                    val key = attributeKeySupplier.get(core)
                    if (key === SKIP_RENDERING) continue
                    val lines = attributeStylizer.stylize(core)
                    ret += AttributeLineFactory.get(key, lines)
                }

                else -> {
                    throw UnsupportedOperationException("${core::class.simpleName} has not yet supported to be rendered")
                }
            }
        }

        return ret
    }

    private inline fun FullKey.onSkipRendering(block: () -> Unit) {
        // TODO use it when kotlin support non-local continue/break
        if (this === SKIP_RENDERING) {
            block()
        }
    }
}

internal class AbilityStylizerImpl : AbilityStylizer {
    override fun stylize(core: BinaryAbilityCore): List<Component> {
        return emptyList() // TODO("implement ability stylizer")
    }
}

internal class AttributeStylizerImpl(
    private val config: RendererConfiguration,
    private val operationStylizer: OperationStylizer,
) : KoinComponent, AttributeStylizer {

    private val mm: MiniMessage by inject(mode = LazyThreadSafetyMode.NONE)

    //<editor-fold desc="Helper implementations for number replacement in attributes">
    /**
     * The default [DecimalFormat] to be used if no one is provided.
     */
    private val defaultDecimalFormat: NumberFormat by lazy { DecimalFormat.getInstance() }

    /**
     * The cache of [DecimalFormat]. The `map key` are patterns for
     * [DecimalFormat].
     */
    private val customDecimalFormats: MutableMap<String, NumberFormat> by reloadable { HashMap() }

    /**
     * Creates a replacement that inserts a number as a component. The
     * component will be formatted by the provided [DecimalFormat].
     *
     * This tag accepts a format pattern, or nothing as arguments.
     *
     * Refer to [DecimalFormat] for usable patterns.
     *
     * This replacement is auto-closing, so its style will not influence the
     * style of following components.
     *
     * @param key the key
     * @param number the number
     * @param operation the operation
     * @return the placeholder
     */
    private fun number(@TagPattern key: String, number: Number, operation: Operation): TagResolver {
        return TagResolver.resolver(key) { args, context ->
            // try to get provided DecimalFormat
            val decimalFormat = if (args.hasNext()) {
                args.pop().value().let { customDecimalFormats.getOrPut(it) { DecimalFormat(it) } }
            } else {
                defaultDecimalFormat
            }
            // format the number for provided DecimalFormat
            decimalFormat.format(number)
                // format the string for provided Operation
                .let { operationStylizer.stylize(it, operation) }
                // create the placeholder
                .let { Tag.inserting(context.deserialize(it)) }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Helper implementations for attack_speed_level attribute">
    private val attackSpeedLevelTagResolvers: MutableMap<Int, TagResolver> by reloadable { Int2ObjectOpenHashMap() }
    private fun getAttackSpeedLevelTagResolver(levelIndex: Int): TagResolver {
        return attackSpeedLevelTagResolvers.getOrPut(levelIndex) {
            component("value", mm.deserialize(config.attackSpeedFormat.levels.getOrThrow(levelIndex)))
        }
    }
    //</editor-fold>

    override fun stylize(core: BinaryAttributeCore): List<Component> {
        // 先拿到 key，获取到对应的 format (String)
        // 把 format 当成 mini string 然后反序列化，同时传入 tag resolvers
        // 注意这里的 tag resolvers 需要根据 format, operation, element 分情况添加

        val key = core.key
        val value = core.data
        val tagResolvers = TagResolver.builder()
        if (key == Attributes.ATTACK_SPEED_LEVEL.key() && value is PlainAttributeData.S) {
            // 单独处理攻击速度
            // 因为攻击速度需要显示为文字
            tagResolvers.resolver(getAttackSpeedLevelTagResolver(value.value.toInt()))
            return listOf(mm.deserialize(config.attackSpeedFormat.merged, tagResolvers.build()))
        } else when (value) {
            // 其余按格式统一处理
            // 因为其余的都直接显示为数字
            is PlainAttributeData.S -> tagResolvers.resolver(
                number("value", value.value, value.operation)
            )

            is PlainAttributeData.R -> tagResolvers.resolvers(
                number("min", value.lower, value.operation),
                number("max", value.upper, value.operation)
            )

            is PlainAttributeData.SE -> tagResolvers.resolvers(
                number("value", value.value, value.operation),
                component("element", value.element.displayName)
            )

            is PlainAttributeData.RE -> tagResolvers.resolvers(
                number("min", value.lower, value.operation),
                number("max", value.upper, value.operation),
                component("element", value.element.displayName)
            )
        }
        return listOf(mm.deserialize(config.attributeFormats.getOrThrow(key), tagResolvers.build()))
    }

    class AttackSpeedFormatImpl(
        override val merged: String,
        override val levels: Map<Int, String>,
    ) : AttributeStylizer.AttackSpeedFormat {
        override fun toString(): String {
            return "AttackSpeedFormat(merged=$merged, levels=${levels.entries.joinToString { it.toString() }})"
        }
    }
}

internal class OperationStylizerImpl(
    private val config: RendererConfiguration,
) : OperationStylizer {
    override fun stylize(value: String, operation: Operation): String = when (operation) {
        Operation.ADD -> config.operationFormats.getOrThrow(Operation.ADD).format(value)
        Operation.MULTIPLY_BASE -> config.operationFormats.getOrThrow(Operation.MULTIPLY_BASE).format(value)
        Operation.MULTIPLY_TOTAL -> config.operationFormats.getOrThrow(Operation.MULTIPLY_TOTAL).format(value)
    }
}

internal class ItemMetaStylizerImpl(
    private val config: RendererConfiguration,
) : KoinComponent, ItemMetaStylizer {

    private val mm: MiniMessage by inject(mode = LazyThreadSafetyMode.NONE)
    private val rarityStyleTagCache: MutableMap<Rarity, Tag> by reloadable { HashMap() }

    override fun stylizeName(item: NekoItemStack): Component {
        val displayName = item.metadata.get<BDisplayNameMeta, _>()
        return if (displayName != null) {
            val resolvers = TagResolver.builder()

            // resolve name
            resolvers.resolver(parsed("value", displayName))

            // resolve rarity style
            val rarity = item.metadata.get<RarityMeta, _>()
            if (rarity != null) {
                rarityStyleTagCache.getOrPut(rarity) {
                    Tag.styling(*rarity.styles)
                }.let {
                    resolvers.tag("rarity_style", it)
                }
            }

            mm.deserialize(config.nameFormat, resolvers.build())
        } else {
            text("Unnamed")
        }
    }

    private val childStylizerMap: Map<KClass<out BinaryItemMeta<*>>, ChildStylizer<*>> = buildMap {
        // Side note: register each in alphabet order

        registerChildStylizer<BDisplayLoreMeta> { displayLoreMeta ->
            val lore = displayLoreMeta.get()
            val header = config.loreFormat.header?.let { it.mapTo(ObjectArrayList(it.size), mm::deserialize) }
            val bottom = config.loreFormat.bottom?.let { it.mapTo(ObjectArrayList(it.size), mm::deserialize) }
            val lines = lore.mapTo(ObjectArrayList(lore.size)) { mm.deserialize(config.loreFormat.line, parsed("line", it)) }
            if (header == null && bottom == null) lines
            else if (header == null && bottom != null) lines.apply { this += bottom }
            else if (header != null && bottom == null) header.apply { this += lines }
            else if (header != null && bottom != null) header.apply { this += lines; this += bottom }
            else error("Should not happen")
        }
        registerChildStylizer<BDurabilityMeta> {
            val durability = it.get()
            val text = mm.deserialize(
                config.durabilityFormat,
                component("threshold", text(durability.threshold)),
                component("damage", text(durability.damage)),
                component("percent", text(durability.damagePercent))
            )
            listOf(text)
        }
        registerChildStylizer<BLevelMeta> { listOf(mm.deserialize(config.levelFormat, component("value", text(it.get())))) }
        registerChildStylizer<BRarityMeta> { listOf(mm.deserialize(config.rarityFormat, component("value", it.get().displayName))) }
        registerChildStylizer<BElementMeta> { stylizeList(it.get(), config.elementFormat, Element::displayName) }
        registerChildStylizer<BKizamiMeta> { stylizeList(it.get(), config.kizamiFormat, Kizami::displayName) }
        registerChildStylizer<BSkinMeta> { listOf(mm.deserialize(config.skinFormat, component("value", it.get().displayName))) }
        registerChildStylizer<BSkinOwnerMeta> { listOf(mm.deserialize(config.skinOwnerFormat, unparsed("value", it.get().toString()))) }
    }

    private inline fun <reified T : BinaryItemMeta<*>> MutableMap<KClass<out BinaryItemMeta<*>>, ChildStylizer<*>>.registerChildStylizer(
        stylizer: ChildStylizer<T>,
    ) {
        this[T::class] = stylizer
    }

    /**
     * A generic function to stylize objects in the
     * [ItemMetaStylizer.ListFormat].
     */
    private inline fun <T> stylizeList(
        collection: Collection<T>,
        listFormat: ItemMetaStylizer.ListFormat,
        placeholderValue: (T) -> Component,
    ): List<Component> {
        val merged = collection
            .mapTo(ObjectArrayList(collection.size)) { mm.deserialize(listFormat.single, component("single", placeholderValue(it))) }
            .join(JoinConfiguration.separator(mm.deserialize(listFormat.separator)))
            .let { mm.deserialize(listFormat.merged, component("merged", it)) }
        return listOf(merged)
    }

    private object DefaultChildStylizer : ChildStylizer<BinaryItemMeta<*>> {
        private val NO_IMPLEMENTATION: MutableMap<KClass<out BinaryItemMeta<*>>, List<Component>> by reloadable { Reference2ObjectLinkedOpenHashMap() }

        override fun stylize(input: BinaryItemMeta<*>): List<Component> {
            return NO_IMPLEMENTATION.getOrPut(input::class) { listOf(text(input::class.simpleName ?: "???")) }
        }
    }

    override fun <I : BinaryItemMeta<*>> getChildStylizerBy(clazz: KClass<out I>): ChildStylizer<I> {
        @Suppress("UNCHECKED_CAST") // Generics suck
        return (childStylizerMap[clazz] ?: DefaultChildStylizer) as ChildStylizer<I>
    }

    class LoreFormatImpl(
        override val line: String,
        override val header: List<String>?,
        override val bottom: List<String>?,
    ) : ItemMetaStylizer.LoreFormat {
        override fun toString(): String {
            return "LoreFormat(line=$line, header=${header?.joinToString()}, bottom=${bottom?.joinToString()})"
        }
    }

    class ListFormatImpl(
        override val merged: String,
        override val single: String,
        override val separator: String,
    ) : ItemMetaStylizer.ListFormat {
        override fun toString(): String {
            return "ListFormat(merged=$merged, single=$single, separator=$separator)"
        }
    }
}