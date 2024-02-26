package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.attribute.base.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.base.Attributes
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeValueLU
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeValueLUE
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeValueS
import cc.mewcraft.wakame.attribute.facade.BinaryAttributeValueSE
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.binary.core.BinaryAbilityCore
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.binary.core.isEmpty
import cc.mewcraft.wakame.item.scheme.meta.*
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.reloadable
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.util.getOrThrow
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
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
import java.util.UUID

// TODO 所有的 stylizer 应该尽可能的实现缓存机制

internal class TextStylizerImpl(
    /* sub stylizers */
    private val metaStylizer: MetaStylizer,
    private val abilityStylizer: AbilityStylizer,
    private val attributeStylizer: AttributeStylizer,

    /* key suppliers */
    private val metaLineKeys: MetaLineKeySupplier,
    private val abilityLineKeys: AbilityLineKeySupplier,
    private val attributeLineKeys: AttributeLineKeySupplier,
) : TextStylizer {
    override fun stylizeName(item: NekoItemStack): Component {
        val name = item.metadata.name
        return if (name != null) {
            metaStylizer.stylizeName(name)
        } else {
            Component.empty()
        }
    }

    override fun stylizeLore(item: NekoItemStack): Collection<LoreLine> {
        val ret = ObjectArrayList<LoreLine>(16)

        // for each meta in neko
        with(item.metadata) {
            lore?.let { ret += MetaLoreLineFactory.get(metaLineKeys.get(DisplayLoreMeta::class), metaStylizer.stylizeLore(it)) }
            level?.let { ret += MetaLoreLineFactory.get(metaLineKeys.get(LevelMeta::class), metaStylizer.stylizeLevel(it)) }
            rarity?.let { ret += MetaLoreLineFactory.get(metaLineKeys.get(RarityMeta::class), metaStylizer.stylizeRarity(it)) }
            element?.let { ret += MetaLoreLineFactory.get(metaLineKeys.get(ElementMeta::class), metaStylizer.stylizeElement(it)) }
            kizami?.let { ret += MetaLoreLineFactory.get(metaLineKeys.get(KizamiMeta::class), metaStylizer.stylizeKizami(it)) }
            skin?.let { ret += MetaLoreLineFactory.get(metaLineKeys.get(SkinMeta::class), metaStylizer.stylizeSkin(it)) }
            skinOwner?.let { ret += MetaLoreLineFactory.get(metaLineKeys.get(SkinOwnerMeta::class), metaStylizer.stylizeSkinOwner(it)) }
        }

        // for each cell in neko
        item.cells.asMap().values.forEach {
            val core = it.binaryCore
            if (core.isEmpty) {
                ret += AttributeLoreLineFactory.empty() // TODO 词条栏系统应该限制可替换的核心类型
            } else {
                when (core) {
                    is BinaryAbilityCore -> ret += AbilityLoreLineFactory.get(abilityLineKeys.get(core), abilityStylizer.stylize(core))
                    is BinaryAttributeCore -> ret += AttributeLoreLineFactory.get(attributeLineKeys.get(core), attributeStylizer.stylize(core))
                    else -> throw UnsupportedOperationException("${core::class.simpleName} has not yet supported to be rendered")
                }
            }
        }

        return ret
    }
}

internal class AbilityStylizerImpl : AbilityStylizer {
    override fun stylize(core: BinaryAbilityCore): List<Component> {
        // TODO("implement ability stylizer")
        return emptyList()
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
    private val defaultDecimalFormat: NumberFormat = DecimalFormat.getInstance()

    /**
     * The cache of [DecimalFormat]. The `map key` are patterns for
     * [DecimalFormat].
     */
    private val customDecimalFormats: MutableMap<String, NumberFormat> by reloadable { Object2ObjectOpenHashMap() }

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
        // 把 format 当成 mini 然后反序列化，同时传入 tag resolvers
        // 注意这里的 tag resolvers 需要根据 format, operation, element 分情况添加

        val key = core.key
        val value = core.value
        val tagResolvers = TagResolver.builder()
        if (key == Attributes.ATTACK_SPEED_LEVEL.key()) {
            /* 单独处理攻击速度 */
            value as BinaryAttributeValueS
            tagResolvers.resolver(getAttackSpeedLevelTagResolver(value.value.toInt()))
            return listOf(mm.deserialize(config.attackSpeedFormat.merged, tagResolvers.build()))
        } else {
            /* 其余按格式统一处理 */
            when (value) {
                is BinaryAttributeValueS -> tagResolvers.resolver(
                    number("value", value.value, value.operation)
                )

                is BinaryAttributeValueLU -> tagResolvers.resolvers(
                    number("min", value.lower, value.operation),
                    number("max", value.upper, value.operation)
                )

                is BinaryAttributeValueSE -> tagResolvers.resolvers(
                    number("value", value.value, value.operation),
                    component("element", value.element.displayNameComponent)
                )

                is BinaryAttributeValueLUE -> tagResolvers.resolvers(
                    number("min", value.lower, value.operation),
                    number("max", value.upper, value.operation),
                    component("element", value.element.displayNameComponent)
                )

                else -> error("Unhandled attribute struct. Missing implementation?")
            }
            return listOf(mm.deserialize(config.attributeFormats.getOrThrow(key), tagResolvers.build()))
        }
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

internal class MetaStylizerImpl(
    private val config: RendererConfiguration,
) : KoinComponent, MetaStylizer {

    private val mm: MiniMessage by inject(mode = LazyThreadSafetyMode.NONE)

    override fun stylizeName(name: String): Component {
        return mm.deserialize(config.nameFormat, parsed("value", name))
    }

    override fun stylizeLore(lore: List<String>): List<Component> {
        val header = config.loreFormat.header?.let { it.mapTo(ObjectArrayList(it.size), mm::deserialize) }
        val bottom = config.loreFormat.bottom?.let { it.mapTo(ObjectArrayList(it.size), mm::deserialize) }
        val lines = lore.mapTo(ObjectArrayList(lore.size)) { mm.deserialize(config.loreFormat.line, parsed("line", it)) }

        return if (header == null && bottom == null) {
            lines
        } else if (header == null && bottom != null) {
            lines.apply { this += bottom }
        } else if (header != null && bottom == null) {
            header.apply { this += lines }
        } else if (header != null && bottom != null) {
            header.apply { this += lines; this += bottom }
        } else {
            throw IllegalStateException("Should not happen")
        }
    }

    override fun stylizeLevel(level: Int): List<Component> {
        return listOf(mm.deserialize(config.levelFormat, component("value", text(level))))
    }

    override fun stylizeRarity(rarity: Rarity): List<Component> {
        return listOf(mm.deserialize(config.rarityFormat, component("value", rarity.displayNameComponent)))
    }

    /**
     * A generic function to stylize objects in the [MetaStylizer.ListFormat].
     */
    private inline fun <T> stylizeList(
        collection: Collection<T>,
        listFormat: MetaStylizer.ListFormat,
        placeholderValue: (T) -> Component,
    ): List<Component> {
        val values = collection.mapTo(ObjectArrayList(collection.size)) {
            mm.deserialize(listFormat.single, component("single", placeholderValue(it)))
        }
        val joined = Component.join(
            JoinConfiguration.separator(
                mm.deserialize(listFormat.separator)
            ), values
        )
        val merged = mm.deserialize(listFormat.merged, component("merged", joined))
        return listOf(merged)
    }

    override fun stylizeElement(elements: Set<Element>): List<Component> {
        return stylizeList(elements, config.elementFormat, Element::displayNameComponent)
    }

    override fun stylizeKizami(kizami: Set<Kizami>): List<Component> {
        return stylizeList(kizami, config.kizamiFormat, Kizami::displayNameComponent)
    }

    override fun stylizeSkin(skin: ItemSkin): List<Component> {
        return listOf(mm.deserialize(config.skinFormat, component("value", skin.displayNameComponent)))
    }

    override fun stylizeSkinOwner(skinOwner: UUID): List<Component> {
        return listOf(mm.deserialize(config.skinOwnerFormat, unparsed("value", skinOwner.toString())))
    }

    class LoreFormatImpl(
        override val line: String,
        override val header: List<String>?,
        override val bottom: List<String>?,
    ) : MetaStylizer.LoreFormat {
        override fun toString(): String {
            return "LoreFormat(line=$line, header=${header?.joinToString()}, bottom=${bottom?.joinToString()})"
        }
    }

    class ListFormatImpl(
        override val merged: String,
        override val single: String,
        override val separator: String,
    ) : MetaStylizer.ListFormat {
        override fun toString(): String {
            return "ListFormat(merged=$merged, single=$single, separator=$separator)"
        }
    }
}