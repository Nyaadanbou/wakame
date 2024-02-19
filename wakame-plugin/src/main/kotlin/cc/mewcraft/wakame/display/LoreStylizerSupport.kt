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
import cc.mewcraft.wakame.registry.AttributeStructType
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.util.getOrThrow
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.ParserDirective
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.TagPattern
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

// TODO 所有的 stylizer 应该尽可能的实现缓存机制

internal class LoreStylizerImpl(
    /* stylizers */
    private val metaStylizer: MetaStylizer,
    private val abilityStylizer: AbilityStylizer,
    private val attributeStylizer: AttributeStylizer,

    /* key suppliers */
    private val metaLineKeys: MetaLineKeySupplier,
    private val abilityLineKeys: AbilityLineKeySupplier,
    private val attributeLineKeys: AttributeLineKeySupplier,
) : LoreStylizer {
    override fun stylize(item: NekoItemStack): Collection<LoreLine> {
        val ret = ObjectArrayList<LoreLine>(8) // TODO estimate the capacity to reduce array copy operations

        // for each meta in neko
        with(item.metadata) {
            lore?.let { ret += MetaLoreLineFactory.get(metaLineKeys.get(LoreMeta::class), metaStylizer.stylizeLore(it)) }
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
                // TODO 渲染空词条栏
            } else {
                when (core) {
                    is BinaryAbilityCore -> ret += AbilityLoreLineFactory.get(abilityLineKeys.get(core), abilityStylizer.stylizeAbility(core))
                    is BinaryAttributeCore -> ret += AttributeLoreLineFactory.get(attributeLineKeys.get(core), attributeStylizer.stylizeAttribute(core))
                    else -> throw UnsupportedOperationException("${core::class.simpleName} has not yet supported to be rendered")
                }
            }
        }

        // TODO considering the lines starting with '/'
        //  (not including the lines with ONLY a '/'

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
     * - 这里的 `map key` 跟 [AttributeRegistry] 里的一致，不是 [FullKey]。
     * - 这里的 `map value` 就是配置文件里对应的字符串值，无需做任何处理。
     *
     * 请注意，该 map 不包含攻击速度！
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

    private fun number(@TagPattern key: String, number: Number, operation: Operation): TagResolver {
        return TagResolver.resolver(key) { argumentQueue: ArgumentQueue, context: Context ->
            val decimalFormat: NumberFormat
            if (argumentQueue.hasNext()) {
                val locale = argumentQueue.pop().value()
                if (argumentQueue.hasNext()) {
                    val format = argumentQueue.pop().value()
                    decimalFormat = DecimalFormat(format, DecimalFormatSymbols(Locale.forLanguageTag(locale)))
                } else {
                    decimalFormat = if (locale.contains(".")) {
                        DecimalFormat(locale, DecimalFormatSymbols.getInstance())
                    } else {
                        DecimalFormat.getInstance(Locale.forLanguageTag(locale))
                    }
                }
            } else {
                decimalFormat = DecimalFormat.getInstance()
            }
            Tag.inserting(context.deserialize(decimalFormat.format(number)))
        }
    }

    override fun stylizeAttribute(core: BinaryAttributeCore): List<Component> {
        // 先拿到 key，获取到对应的 format (String)
        // 把 format 当成 mini 然后反序列化，同时传入 tag resolvers
        // 注意这里的 tag resolvers 需要根据 format, operation, element 分情况添加

        val key = core.key
        val value = core.value

        // val format = value.format
        val operation = value.operation // TODO consider modifier operation
        // val element = value.elementOrNull

        val tagResolvers = TagResolver.builder()

        if (core.key == Attributes.ATTACK_SPEED_LEVEL.key()) {
            /* 单独渲染攻击速度 */

            value as BinaryAttributeValueS<*>
            val level = attackSpeedFormat.levels[value.value.toInt()]
            tagResolvers.resolver(Placeholder.parsed("value", level))
            return listOf(miniMessage.deserialize(attackSpeedFormat.merged, tagResolvers.build()))

        } else {
            /* 其余按格式统一渲染 */

            when (core.value.structType) {
                AttributeStructType.SINGLE -> {
                    value as BinaryAttributeValueS<*>
                    tagResolvers.resolver(
                        Formatter.number("value", value.value) // FIXME 用 Formatter.number 渲染的话，就没办法加百分号了，除非自己写个 TagResolver
                    )
                }

                AttributeStructType.RANGED -> {
                    value as BinaryAttributeValueLU<*>
                    tagResolvers.resolvers(
                        Formatter.number("min", value.lower),
                        Formatter.number("max", value.upper)
                    )
                }

                AttributeStructType.SINGLE_ELEMENT -> {
                    value as BinaryAttributeValueSE<*>
                    tagResolvers.resolvers(
                        Formatter.number("value", value.value),
                        Placeholder.parsed("element", value.element.displayName)
                    )
                }

                AttributeStructType.RANGED_ELEMENT -> {
                    value as BinaryAttributeValueLUE<*>
                    tagResolvers.resolvers(
                        Formatter.number("min", value.lower),
                        Formatter.number("max", value.upper),
                        Placeholder.parsed("element", value.element.displayName)
                    )
                }
            }

            return listOf(miniMessage.deserialize(attributeFormats.getOrThrow(key), tagResolvers.build()))
        }
    }
}

internal class OperationStylizerImpl(
    private val operationFormats: Map<Operation, String>,
) : OperationStylizer {
    override fun stylizeValue(value: Double, operation: Operation): String = when (operation) {
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
        return miniMessage.deserialize(nameFormat, Placeholder.parsed("name", name))
    }

    override fun stylizeLore(lore: List<String>): List<Component> {
        val header = loreFormat.header?.mapTo(ObjectArrayList(loreFormat.header.size)) { miniMessage.deserialize(it) }
        val bottom = loreFormat.bottom?.mapTo(ObjectArrayList(loreFormat.bottom.size)) { miniMessage.deserialize(it) }
        val lines = lore.mapTo(ObjectArrayList(lore.size)) { miniMessage.deserialize(loreFormat.line, Placeholder.parsed("line", it)) }

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
        return listOf(miniMessage.deserialize(levelFormat, Formatter.number("level", level)))
    }

    override fun stylizeRarity(rarity: Rarity): List<Component> {
        return listOf(miniMessage.deserialize(rarityFormat, Placeholder.parsed("rarity", rarity.displayName)))
    }

    /**
     * A generic function to stylize any list of objects.
     */
    private inline fun <T> stylizeList(
        collection: Collection<T>,
        listFormat: MetaStylizer.ListFormat,
        placeholderKey: String,
        placeholderValue: (T) -> String,
    ): List<Component> {
        val values = collection.mapTo(ObjectArrayList(collection.size)) {
            miniMessage.deserialize(listFormat.single, Placeholder.parsed(placeholderKey, placeholderValue(it)))
        }
        val joined = Component.join(
            JoinConfiguration.separator(
                miniMessage.deserialize(listFormat.separator)
            ), values
        )
        val merged = miniMessage.deserialize(listFormat.merged, Placeholder.component("merged", joined))
        return listOf(merged)
    }

    override fun stylizeElement(elements: Set<Element>): List<Component> {
        return stylizeList(elements, elementFormat, "element", Element::displayName)
    }

    override fun stylizeKizami(kizami: Set<Kizami>): List<Component> {
        return stylizeList(kizami, kizamiFormat, "kizami", Kizami::displayName)
    }

    override fun stylizeSkin(skin: ItemSkin): List<Component> {
        return listOf(miniMessage.deserialize(skinFormat, Placeholder.parsed("skin", skin.displayName)))
    }

    override fun stylizeSkinOwner(skinOwner: UUID): List<Component> {
        return listOf(miniMessage.deserialize(skinOwnerFormat, Placeholder.parsed("skin_owner", skinOwner.toString())))
    }
}