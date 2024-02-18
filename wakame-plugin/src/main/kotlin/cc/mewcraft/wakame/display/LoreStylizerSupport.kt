package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.attribute.base.AttributeModifier.Operation
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.binary.core.BinaryAbilityCore
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.binary.core.isEmpty
import cc.mewcraft.wakame.item.scheme.meta.*
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.util.getOrThrow
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.UUID

// TODO 所有的 stylizer 应该尽可能的实现缓存机制

internal class LoreStylizerImpl(
    /* stylizers */
    private val metaStylizer: MetaStylizer,
    private val abilityStylizer: AbilityStylizer,
    private val attributeStylizer: AttributeStylizer,

    /* key suppliers */
    private val metaLineKeySupplier: MetaLineKeySupplier,
    private val abilityLineKeySupplier: AbilityLineKeySupplier,
    private val attributeLineKeySupplier: AttributeLineKeySupplier,
) : LoreStylizer {
    override fun stylize(item: NekoItemStack): Collection<LoreLine> {
        val ret = ObjectArrayList<LoreLine>(8) // TODO estimate the capacity to reduce array copy operations

        // for each meta in neko
        with(item.metadata) {
            lore?.let { ret += MetaLoreLineFactory.get(metaLineKeySupplier.getKey(LoreMeta::class), metaStylizer.stylizeLore(it)) }
            level?.let { ret += MetaLoreLineFactory.get(metaLineKeySupplier.getKey(LevelMeta::class), metaStylizer.stylizeLevel(it)) }
            rarity?.let { ret += MetaLoreLineFactory.get(metaLineKeySupplier.getKey(RarityMeta::class), metaStylizer.stylizeRarity(it)) }
            element?.let { ret += MetaLoreLineFactory.get(metaLineKeySupplier.getKey(ElementMeta::class), metaStylizer.stylizeElement(it)) }
            kizami?.let { ret += MetaLoreLineFactory.get(KizamiMeta.key(), metaStylizer.stylizeKizami(it)) }
            skin?.let { ret += MetaLoreLineFactory.get(SkinMeta.key(), metaStylizer.stylizeSkin(it)) }
            skinOwner?.let { ret += MetaLoreLineFactory.get(SkinOwnerMeta.key(), metaStylizer.stylizeSkinOwner(it)) }
        }

        // for each cell in neko
        item.cells.asMap().values.forEach {
            val core = it.binaryCore
            if (core.isEmpty) {
                // TODO 渲染空词条栏
            } else {
                when (core) {
                    is BinaryAbilityCore -> ret += AbilityLoreLineImpl(abilityLineKeySupplier.getKey(core), abilityStylizer.stylizeAbility(core))
                    is BinaryAttributeCore -> ret += AttributeLoreLineImpl(attributeLineKeySupplier.getKey(core), attributeStylizer.stylizeAttribute(core))
                    else -> throw UnsupportedOperationException("${core::class.simpleName} has not yet supported to be rendered")
                }
            }
        }

        // TODO considering the lines starting with @
        //  (not including the lines with only a @)

        return ret
    }
}

internal class AbilityStylizerImpl : AbilityStylizer {
    override fun stylizeAbility(ability: BinaryAbilityCore): List<Component> {
        TODO("Not yet implemented")
    }
}

internal class AttributeStylizerImpl(
    private val attributeFormats: Map<Key, String>,
    private val operationStylizer: OperationStylizer,
) : AttributeStylizer {
    override fun stylizeAttribute(attribute: BinaryAttributeCore): List<Component> {
        val value = attribute.value
        val operation = value.operation
        TODO("Not yet implemented")
    }
}

internal class OperationStylizerImpl(
    private val operationFormats: Map<Operation, String>,
) : OperationStylizer {
    override fun stylizeValue(value: Double, operation: Operation): String = when (operation) {
        Operation.ADDITION -> operationFormats.getOrThrow(Operation.ADDITION).format(value)
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

    override fun stylizeElement(elements: Set<Element>): List<Component> {
        val values = elements.mapTo(ObjectArrayList(elements.size)) {
            miniMessage.deserialize(elementFormat.single, Placeholder.parsed("element", it.displayName))
        }
        val joined = Component.join(
            JoinConfiguration.separator(
                miniMessage.deserialize(elementFormat.separator)
            ), values
        )
        val merged = miniMessage.deserialize(elementFormat.merged, Placeholder.component("merged", joined))
        return listOf(merged)
    }

    override fun stylizeKizami(kizami: Set<Kizami>): List<Component> {
        val values = kizami.mapTo(ObjectArrayList(kizami.size)) {
            miniMessage.deserialize(kizamiFormat.single, Placeholder.parsed("kizami", it.displayName))
        }
        val joined = Component.join(
            JoinConfiguration.separator(
                miniMessage.deserialize(kizamiFormat.separator)
            ), values
        )
        val merged = miniMessage.deserialize(kizamiFormat.merged, Placeholder.component("merged", joined))
        return listOf(merged)
    }

    override fun stylizeSkin(skin: ItemSkin): List<Component> {
        return listOf(miniMessage.deserialize(skinFormat, Placeholder.parsed("skin", skin.displayName)))
    }

    override fun stylizeSkinOwner(skinOwner: UUID): List<Component> {
        return listOf(miniMessage.deserialize(skinOwnerFormat, Placeholder.parsed("skin_owner", skinOwner.toString())))
    }
}