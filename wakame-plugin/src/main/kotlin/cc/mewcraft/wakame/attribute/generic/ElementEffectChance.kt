package cc.mewcraft.wakame.attribute.generic

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.FixedAttribute
import cc.mewcraft.wakame.attribute.ItemAttribute
import cc.mewcraft.wakame.attribute.PlayerAttribute
import cc.mewcraft.wakame.item.SlotContent
import cc.mewcraft.wakame.item.TagSerializable
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.ByteBinaryTag

/**
 * 元素触发概率 %
 */
class ElementEffectChance(
    var id: String,
    base: Int,
) : FixedAttribute(base), ItemAttribute, PlayerAttribute, TagSerializable {
    private val key = Key.key(SlotContent.ATTRIBUTE_NAMESPACE, "${id}_effect_chance")

    override fun key(): Key {
        return key
    }

    override fun asAttributeModifier(): AttributeModifier {
        return AttributeModifier(value, AttributeModifier.Operation.ADDITION)
    }

    // Byte

    override fun save(): BinaryTag {
        return ByteBinaryTag.byteBinaryTag(value.toByte())
    }

    override fun load(tag: BinaryTag) {
        if (tag is ByteBinaryTag)
            base = tag.intValue()
    }
}