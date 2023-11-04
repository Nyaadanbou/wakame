package cc.mewcraft.wakame.attribute.generic

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.FixedAttribute
import cc.mewcraft.wakame.attribute.ItemAttribute
import cc.mewcraft.wakame.attribute.PlayerAttribute
import cc.mewcraft.wakame.item.SlotContent
import cc.mewcraft.wakame.item.TagSerializable
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.FloatBinaryTag

/**
 * 最大生命值
 */
class MaximumHealth(
    base: Int,
) : FixedAttribute(base), ItemAttribute, PlayerAttribute, TagSerializable {
    companion object {
        val KEY = Key.key(SlotContent.ATTRIBUTE_NAMESPACE, "maximum_health")
    }

    override fun key(): Key {
        return KEY
    }

    override fun asAttributeModifier(): AttributeModifier {
        return AttributeModifier(value, AttributeModifier.Operation.ADDITION)
    }

    // Float

    override fun save(): BinaryTag {
        return FloatBinaryTag.floatBinaryTag(value.toFloat())
    }

    override fun load(tag: BinaryTag) {
        if (tag is FloatBinaryTag)
            base = tag.intValue()
    }
}