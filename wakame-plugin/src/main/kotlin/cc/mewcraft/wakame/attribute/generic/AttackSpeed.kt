package cc.mewcraft.wakame.attribute.generic

import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.item.TagSerializable
import cc.mewcraft.wakame.item.SlotContent
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.ByteBinaryTag


/**
 * 武器攻击速度
 *
 * The states of Attack Speed is finite. That is, there are only 7 different attack speeds:
 * - (0) Super slow
 * - (1) Very slow
 * - (2) Slow
 * - (3) Normal
 * - (4) Fast
 * - (5) Very fast
 * - (6) Super fast
 */
class AttackSpeed(base: Int) : FixedAttribute(base), ItemAttribute, PlayerAttribute, TagSerializable {
    companion object {
        val KEY = Key.key(SlotContent.ATTRIBUTE_NAMESPACE, "attack_speed")
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