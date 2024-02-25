package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.MINIMESSAGE_FULL
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
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.util.getOrThrow
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
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
import org.koin.core.qualifier.named
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
                    is BinaryAbilityCore -> ret += AbilityLoreLineFactory.get(abilityLineKeys.get(core), abilityStylizer.stylizeAbility(core))
                    is BinaryAttributeCore -> ret += AttributeLoreLineFactory.get(attributeLineKeys.get(core), attributeStylizer.stylizeAttribute(core))
                    else -> throw UnsupportedOperationException("${core::class.simpleName} has not yet supported to be rendered")
                }
            }
        }

        return ret
    }
}

internal class AbilityStylizerImpl : AbilityStylizer {
    override fun stylizeAbility(core: BinaryAbilityCore): List<Component> {
        // TODO("implement ability stylizer")
        return emptyList()
    }
}

internal class AttributeStylizerImpl(
    /**
     * ## Keys and values
     * - 这里的 `map key` 跟 [AttributeRegistry] 里的一致，不是 [FullKey]
     * - 这里的 `map value` 就是配置文件里对应的字符串值，无需做任何处理
     *
     * 注意该 map 不包含 `attack_speed_level` (攻击速度).
     */
    private val attributeFormats: Map<Key, String>,
    /**
     * 攻击速度的渲染格式。
     */
    private val attackSpeedFormat: AttributeStylizer.AttackSpeedFormat,
    /**
     * 运算模式的渲染实现。
     */
    private val operationStylizer: OperationStylizer,
) : KoinComponent, AttributeStylizer {

    private val miniMessage: MiniMessage by inject(named(MINIMESSAGE_FULL))

    //<editor-fold desc="Helper implementations for number replacement in attributes">
    /**
     * The default [DecimalFormat] to be used if no one is provided.
     */
    private val defaultDecimalFormat: NumberFormat = DecimalFormat.getInstance()

    /**
     * The cache of [DecimalFormat]. The `map key` are patterns for
     * [DecimalFormat].
     */
    private val customDecimalFormats: MutableMap<String, NumberFormat> = Object2ObjectOpenHashMap()

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
                .let { operationStylizer.stylizeValue(it, operation) }
                // create the placeholder
                .let { Tag.inserting(context.deserialize(it)) }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Helper implementations for attack_speed_level attribute">
    private val attackSpeedLevelTagResolvers: MutableMap<Int, TagResolver> = Int2ObjectOpenHashMap()
    private fun getAttackSpeedLevelTagResolver(levelIndex: Int): TagResolver {
        return attackSpeedLevelTagResolvers.getOrPut(levelIndex) {
            component("value", miniMessage.deserialize(attackSpeedFormat.levels.getOrThrow(levelIndex)))
        }
    }
    //</editor-fold>

    override fun stylizeAttribute(core: BinaryAttributeCore): List<Component> {
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
            return listOf(miniMessage.deserialize(attackSpeedFormat.merged, tagResolvers.build()))
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
            return listOf(miniMessage.deserialize(attributeFormats.getOrThrow(key), tagResolvers.build()))
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
    private val operationFormats: Map<Operation, String>,
) : OperationStylizer {
    override fun stylizeValue(value: String, operation: Operation): String = when (operation) {
        Operation.ADD -> operationFormats.getOrThrow(Operation.ADD).format(value)
        Operation.MULTIPLY_BASE -> operationFormats.getOrThrow(Operation.MULTIPLY_BASE).format(value)
        Operation.MULTIPLY_TOTAL -> operationFormats.getOrThrow(Operation.MULTIPLY_TOTAL).format(value)
    }
}

internal class MetaStylizerImpl(
    override val nameFormat: String,
    override val loreFormat: MetaStylizer.LoreFormat,
    override val levelFormat: String,
    override val rarityFormat: String,
    override val elementFormat: MetaStylizer.ListFormat,
    override val kizamiFormat: MetaStylizer.ListFormat,
    override val skinFormat: String,
    override val skinOwnerFormat: String,
) : KoinComponent, MetaStylizer {

    private val miniMessage: MiniMessage by inject(named(MINIMESSAGE_FULL))

    override fun stylizeName(name: String): Component {
        return miniMessage.deserialize(nameFormat, parsed("value", name))
    }

    override fun stylizeLore(lore: List<String>): List<Component> {
        val header = loreFormat.header?.let { it.mapTo(ObjectArrayList(it.size), miniMessage::deserialize) }
        val bottom = loreFormat.bottom?.let { it.mapTo(ObjectArrayList(it.size), miniMessage::deserialize) }
        val lines = lore.mapTo(ObjectArrayList(lore.size)) { miniMessage.deserialize(loreFormat.line, parsed("line", it)) }

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
        return listOf(miniMessage.deserialize(levelFormat, component("value", text(level))))
    }

    override fun stylizeRarity(rarity: Rarity): List<Component> {
        return listOf(miniMessage.deserialize(rarityFormat, component("value", rarity.displayNameComponent)))
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
            miniMessage.deserialize(listFormat.single, component("single", placeholderValue(it)))
        }
        val joined = Component.join(
            JoinConfiguration.separator(
                miniMessage.deserialize(listFormat.separator)
            ), values
        )
        val merged = miniMessage.deserialize(listFormat.merged, component("merged", joined))
        return listOf(merged)
    }

    override fun stylizeElement(elements: Set<Element>): List<Component> {
        return stylizeList(elements, elementFormat, Element::displayNameComponent)
    }

    override fun stylizeKizami(kizami: Set<Kizami>): List<Component> {
        return stylizeList(kizami, kizamiFormat, Kizami::displayNameComponent)
    }

    override fun stylizeSkin(skin: ItemSkin): List<Component> {
        return listOf(miniMessage.deserialize(skinFormat, component("value", skin.displayNameComponent)))
    }

    override fun stylizeSkinOwner(skinOwner: UUID): List<Component> {
        return listOf(miniMessage.deserialize(skinOwnerFormat, unparsed("value", skinOwner.toString())))
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