package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeData
import cc.mewcraft.wakame.display.ItemMetaStylizer.ChildStylizer
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.cell.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.binary.cell.core.BinarySkillCore
import cc.mewcraft.wakame.item.binary.cell.core.isEmpty
import cc.mewcraft.wakame.item.binary.meta
import cc.mewcraft.wakame.item.binary.meta.*
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.reloadable
import cc.mewcraft.wakame.util.toSimpleString
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.extra.kotlin.style
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.TagPattern
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.*
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.stream.Stream
import kotlin.collections.set
import kotlin.reflect.KClass

// TODO 所有的 stylizer 应该尽可能的实现缓存机制

internal class TextStylizerImpl(
    /* sub stylizers */
    private val itemMetaStylizer: ItemMetaStylizer,
    private val skillStylizer: SkillStylizer,
    private val attributeStylizer: AttributeStylizer,

    /* full key suppliers */
    private val itemMetaKeySupplier: ItemMetaKeySupplier,
    private val skillKeySupplier: SkillKeySupplier,
    private val attributeKeySupplier: AttributeKeySupplier,
) : TextStylizer {
    override fun stylizeName(item: NekoStack): Component {
        return itemMetaStylizer.stylizeName(item)
    }

    override fun stylizeLore(item: NekoStack): Collection<LoreLine> {
        val ret = ObjectArrayList<LoreLine>(16)

        // for each meta in the item
        for (itemMeta in item.meta.snapshot) {
            if (itemMeta is BDisplayNameMeta) {
                continue // skipping as displayName has been rendered separately
            }

            val key = itemMetaKeySupplier.get(itemMeta)
            if (key === SKIP_RENDERING) continue
            val lines = itemMetaStylizer.getChildStylizerBy(itemMeta).stylize(itemMeta)
            val wrapped = ItemMetaLineFactory.get(key, lines)
            ret += wrapped
        }

        // for each cell in the item
        for (cell in item.cell.snapshot.values) {
            val core = cell.binaryCore
            if (core.isEmpty) {
                // it's an empty core - add the pre-defined placeholder lines

                ret += AttributeLineFactory.empty() // TODO 词条栏系统应该限制可替换的核心类型
            } else when (core) {
                // it's a non-empty core - dynamically generate the lines

                is BinarySkillCore -> {
                    val key = skillKeySupplier.get(core)
                    if (key === SKIP_RENDERING) continue
                    val lines = skillStylizer.stylize(core)
                    ret += SkillLineFactory.get(key, lines)
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

internal class SkillStylizerImpl : SkillStylizer {
    override fun stylize(core: BinarySkillCore): List<Component> {
        return emptyList() // TODO("implement skill stylizer")
    }
}

internal class AttributeStylizerImpl(
    private val config: RendererConfiguration,
    private val operationStylizer: OperationStylizer,
) : KoinComponent, AttributeStylizer {

    private val mm: MiniMessage by inject()

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
            component("value", mm.deserialize(config.attackSpeedFormat.levels.getValue(levelIndex)))
        }
    }
    //</editor-fold>

    override fun stylize(core: BinaryAttributeCore): List<Component> {
        // 先拿到 key，获取到对应的 format (String)
        // 把 format 当成 mini string 然后反序列化，同时传入 tag resolvers
        // 注意这里的 tag resolvers 需要根据 format, operation, element 分情况添加

        val key = core.key
        val value = core.data
        val resolvers = TagResolver.builder()

        when {
            // 单独处理攻击速度，因为需要显示为文字
            key == Attributes.ATTACK_SPEED_LEVEL.key() && value is BinaryAttributeData.S -> {
                resolvers.resolver(getAttackSpeedLevelTagResolver(value.value.toInt()))
                return listOf(mm.deserialize(config.attackSpeedFormat.merged, resolvers.build()))
            }

            // 其他需要单独处理的属性继续写在这里 ...
            // ... -> {
            //     ...
            // }

            // 其余属性都是数字形式，因此统一处理
            else -> when (value) {
                is BinaryAttributeData.S -> {
                    resolvers.resolver(
                        number("value", value.value, value.operation)
                    )
                }

                is BinaryAttributeData.R -> {
                    resolvers.resolvers(
                        number("min", value.lower, value.operation),
                        number("max", value.upper, value.operation)
                    )
                }

                is BinaryAttributeData.SE -> {
                    resolvers.resolvers(
                        number("value", value.value, value.operation),
                        component("element", value.element.displayName)
                    )
                }

                is BinaryAttributeData.RE -> {
                    resolvers.resolvers(
                        number("min", value.lower, value.operation),
                        number("max", value.upper, value.operation),
                        component("element", value.element.displayName)
                    )
                }
            }
        }

        return listOf(mm.deserialize(config.attributeFormats.getValue(key), resolvers.build()))
    }

    class AttackSpeedFormatImpl(
        override val merged: String,
        override val levels: Map<Int, String>,
    ) : AttributeStylizer.AttackSpeedFormat {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("merged", merged),
            ExaminableProperty.of("levels", levels)
        )

        override fun toString(): String = toSimpleString()
    }
}

internal class OperationStylizerImpl(
    private val config: RendererConfiguration,
) : OperationStylizer {
    override fun stylize(value: String, operation: Operation): String = when (operation) {
        Operation.ADD -> config.operationFormats.getValue(Operation.ADD).format(value)
        Operation.MULTIPLY_BASE -> config.operationFormats.getValue(Operation.MULTIPLY_BASE).format(value)
        Operation.MULTIPLY_TOTAL -> config.operationFormats.getValue(Operation.MULTIPLY_TOTAL).format(value)
    }
}

internal class ItemMetaStylizerImpl(
    private val config: RendererConfiguration,
) : KoinComponent, ItemMetaStylizer {

    private val mm: MiniMessage by inject()
    private val rarityStyleMap: LoadingCache<Rarity, Tag> by reloadable {
        Caffeine.newBuilder().build { rarity -> Tag.styling(*rarity.styles) }
    }

    override fun stylizeName(item: NekoStack): Component {
        val displayName = item.meta<BDisplayNameMeta>().getOrNull() ?: return text("Unnamed")

        val resolvers = TagResolver.builder().apply {
            // resolve name
            resolver(parsed("value", displayName))
            // resolve rarity style
            item.meta<BRarityMeta>().getOrNull()?.run {
                tag("rarity_style", rarityStyleMap[this])
            }
        }

        return mm.deserialize(config.nameFormat, resolvers.build())
    }

    private val childStylizerMap: Map<KClass<out BinaryItemMeta<*>>, ChildStylizer<*>> = buildMap {
        // Side note: register each child stylizer in alphabet order

        // Add more child stylizer here ...

        registerChildStylizer<BDisplayLoreMeta> {
            val lore = get()
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
            val durability = get()
            val text = mm.deserialize(
                config.durabilityFormat,
                component("threshold", text(durability.threshold)),
                component("damage", text(durability.damage)),
                component("percent", text(durability.damagePercent))
            )
            listOf(text)
        }
        registerChildStylizer<BLevelMeta> {
            listOf(mm.deserialize(config.levelFormat, component("value", text(get()))))
        }
        registerChildStylizer<BRarityMeta> {
            listOf(mm.deserialize(config.rarityFormat, component("value", get().displayName)))
        }
        registerChildStylizer<BElementMeta> {
            stylizeList(get(), config.elementFormat, Element::displayName)
        }
        registerChildStylizer<BKizamiMeta> {
            stylizeList(get(), config.kizamiFormat, Kizami::displayName)
        }
        registerChildStylizer<BSkinMeta> {
            listOf(mm.deserialize(config.skinFormat, component("value", get().displayName)))
        }
        registerChildStylizer<BSkinOwnerMeta> {
            listOf(mm.deserialize(config.skinOwnerFormat, unparsed("value", get().toString())))
        }
    }

    private inline fun <reified T : BinaryItemMeta<*>> MutableMap<KClass<out BinaryItemMeta<*>>, ChildStylizer<*>>.registerChildStylizer(
        crossinline stylizer: T.() -> List<Component>,
    ) {
        this[T::class] = ChildStylizer<T> { stylizer.invoke(it) }
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
        private val NOOP_IMPLEMENTATION: MutableMap<KClass<out BinaryItemMeta<*>>, List<Component>> by reloadable { Reference2ObjectLinkedOpenHashMap() }

        override fun stylize(input: BinaryItemMeta<*>): List<Component> {
            return NOOP_IMPLEMENTATION.getOrPut(input::class) {
                listOf(
                    text(input::class.simpleName ?: "???", style {
                        color(NamedTextColor.WHITE)
                        decoration(TextDecoration.ITALIC, false)
                    })
                )
            }
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
            return toSimpleString()
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("line", line),
                ExaminableProperty.of("header", header),
                ExaminableProperty.of("bottom", bottom)
            )
        }
    }

    class ListFormatImpl(
        override val merged: String,
        override val single: String,
        override val separator: String,
    ) : ItemMetaStylizer.ListFormat {
        override fun toString(): String {
            return toSimpleString()
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(
                ExaminableProperty.of("merged", merged),
                ExaminableProperty.of("single", single),
                ExaminableProperty.of("separator", separator)
            )
        }
    }
}