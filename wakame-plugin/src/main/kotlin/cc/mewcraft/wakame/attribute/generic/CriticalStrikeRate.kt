package cc.mewcraft.wakame.attribute.generic

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.FixedAttribute
import cc.mewcraft.wakame.attribute.ItemAttribute
import cc.mewcraft.wakame.attribute.PlayerAttribute
import cc.mewcraft.wakame.item.SlotContent
import cc.mewcraft.wakame.item.TagSerializable
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.ShortBinaryTag

/**
 * 暴击倍率 %
 */
class CriticalStrikeRate(
    base: Int,
) : FixedAttribute(base), ItemAttribute, PlayerAttribute, TagSerializable {
    companion object {
        val KEY = Key.key(SlotContent.ATTRIBUTE_NAMESPACE, "critical_strike_rate")
    }

    override fun key(): Key {
        return KEY
    }

    override fun asAttributeModifier(): AttributeModifier {
        return AttributeModifier(value, AttributeModifier.Operation.ADDITION)
    }

    // Short

    override fun save(): BinaryTag =
        ShortBinaryTag.shortBinaryTag(value.toShort())

    override fun load(tag: BinaryTag) {
        if (tag is ShortBinaryTag)
            base = tag.intValue()
    }
}