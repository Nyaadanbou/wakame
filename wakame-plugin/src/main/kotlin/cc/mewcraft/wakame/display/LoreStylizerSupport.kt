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

internal class LoreStylizerImpl(
    private val metaStylizer: MetaStylizer,
    private val abilityStylizer: AbilityStylizer,
    private val attributeStylizer: AttributeStylizer,
) : LoreStylizer {
    override fun stylize(nekoItemStack: NekoItemStack): Collection<LoreLine> {
        val ret = ObjectArrayList<LoreLine>(8) // TODO estimate the capacity to reduce array copy operations

        // for each meta in neko
        with(nekoItemStack.itemMeta) {
            lore?.let { ret += MetaLoreLineImpl(LoreMeta.key().asString(), metaStylizer.stylizeLore(it)) }
            level?.let { ret += MetaLoreLineImpl(LevelMeta.key().asString(), metaStylizer.stylizeLevel(it)) }
            rarity?.let { ret += MetaLoreLineImpl(RarityMeta.key().asString(), metaStylizer.stylizeRarity(it)) }
            element?.let { ret += MetaLoreLineImpl(ElementMeta.key().asString(), metaStylizer.stylizeElement(it)) }
            kizami?.let { ret += MetaLoreLineImpl(KizamiMeta.key().asString(), metaStylizer.stylizeKizami(it)) }
            skin?.let { ret += MetaLoreLineImpl(SkinMeta.key().asString(), metaStylizer.stylizeSkin(it)) }
            skinOwner?.let { ret += MetaLoreLineImpl(SkinOwnerMeta.key().asString(), metaStylizer.stylizeSkinOwner(it)) }
        }

        // for each cell in neko
        nekoItemStack.cells.asMap().values.forEach {
            val core = it.binaryCore
            if (core.isEmpty) {
                // TODO 渲染空词条栏
            } else {
                when (core) {
                    is BinaryAbilityCore -> ret += AbilityLoreLineImpl(core.key.asString(), abilityStylizer.stylizeAbility(core))
                    is BinaryAttributeCore -> ret += AttributeLoreLineImpl(core.key.asString(), attributeStylizer.stylizeAttribute(core))
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
    private val map: Map<Key, String>,
    private val operationStylizer: OperationStylizer,
) : AttributeStylizer {
    override fun stylizeAttribute(attribute: BinaryAttributeCore): List<Component> {
        TODO("Not yet implemented")
    }
}

internal class OperationStylizerImpl(
    private val formats: Map<Operation, String>,
) : OperationStylizer {
    override fun stylizeAddition(value: Double): String = formats.getOrThrow(Operation.ADDITION).format(value)
    override fun stylizeMultiplyBase(value: Double): String = formats.getOrThrow(Operation.MULTIPLY_BASE).format(value)
    override fun stylizeMultiplyTotal(value: Double): String = formats.getOrThrow(Operation.MULTIPLY_TOTAL).format(value)
}

internal class MetaStylizerImpl(
    override val nameStyle: String,
    override val loreStyle: MetaStylizer.LoreStyle,
    override val levelStyle: String,
    override val rarityStyle: String,
    override val elementStyle: MetaStylizer.ListStyle,
    override val kizamiStyle: MetaStylizer.ListStyle,
    override val skinStyle: String,
    override val skinOwnerStyle: String,
) : KoinComponent, MetaStylizer {

    private val miniMessage: MiniMessage by inject(named(MINIMESSAGE_FULL))

    override fun stylizeName(name: String): Component {
        return miniMessage.deserialize(nameStyle, Placeholder.parsed("name", name))
    }

    override fun stylizeLore(lore: List<String>): List<Component> {
        val header = loreStyle.header?.mapTo(ObjectArrayList(loreStyle.header.size)) { miniMessage.deserialize(it) }
        val bottom = loreStyle.bottom?.mapTo(ObjectArrayList(loreStyle.bottom.size)) { miniMessage.deserialize(it) }
        val lines = lore.mapTo(ObjectArrayList(lore.size)) { miniMessage.deserialize(loreStyle.line, Placeholder.parsed("line", it)) }

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
        return listOf(miniMessage.deserialize(levelStyle, Formatter.number("level", level)))
    }

    override fun stylizeRarity(rarity: Rarity): List<Component> {
        return listOf(miniMessage.deserialize(rarityStyle, Placeholder.parsed("rarity", rarity.displayName)))
    }

    override fun stylizeElement(elements: Set<Element>): List<Component> {
        val values = elements.mapTo(ObjectArrayList(elements.size)) {
            miniMessage.deserialize(elementStyle.single, Placeholder.parsed("element", it.displayName))
        }
        val merged = Component.join(
            JoinConfiguration.separator(
                miniMessage.deserialize(elementStyle.separator)
            ), values
        )
        return listOf(merged)
    }

    override fun stylizeKizami(kizami: Set<Kizami>): List<Component> {
        val values = kizami.mapTo(ObjectArrayList(kizami.size)) {
            miniMessage.deserialize(kizamiStyle.single, Placeholder.parsed("kizami", it.displayName))
        }
        val merged = Component.join(
            JoinConfiguration.separator(
                miniMessage.deserialize(kizamiStyle.separator)
            ), values
        )
        return listOf(merged)
    }

    override fun stylizeSkin(skin: ItemSkin): List<Component> {
        return listOf(miniMessage.deserialize(skinStyle, Placeholder.parsed("skin", skin.displayName)))
    }

    override fun stylizeSkinOwner(skinOwner: UUID): List<Component> {
        return listOf(miniMessage.deserialize(skinOwnerStyle, Placeholder.parsed("skin_owner", skinOwner.toString())))
    }
}