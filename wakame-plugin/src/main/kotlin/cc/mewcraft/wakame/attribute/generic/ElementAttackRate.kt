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
 * 元素攻击倍率 %
 */
class ElementAttackRate(
    var id: String,
    base: Int,
) : FixedAttribute(base), ItemAttribute, PlayerAttribute, TagSerializable {
    private val key = Key.key(SlotContent.ATTRIBUTE_NAMESPACE, "${id}_attack_rate")

    override fun key(): Key {
        return key
    }

    override fun asAttributeModifier(): AttributeModifier {
        return AttributeModifier(value, AttributeModifier.Operation.ADDITION)
    }

    // Short

    override fun save(): BinaryTag {
        return ShortBinaryTag.shortBinaryTag(value.toShort())
    }

    override fun load(tag: BinaryTag) {
        if (tag is ShortBinaryTag)
            base = tag.intValue()
    }
}