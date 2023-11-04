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
 * 魔法消耗减免 %
 */
class ManaReduction(
    base: Int,
) : FixedAttribute(base), ItemAttribute, PlayerAttribute, TagSerializable {
    companion object {
        val KEY = Key.key(SlotContent.ATTRIBUTE_NAMESPACE, "mana_reduction")
    }

    override fun key(): Key {
        return KEY
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