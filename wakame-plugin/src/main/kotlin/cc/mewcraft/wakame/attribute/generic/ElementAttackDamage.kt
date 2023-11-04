package cc.mewcraft.wakame.attribute.generic

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.ItemAttribute
import cc.mewcraft.wakame.attribute.PlayerAttribute
import cc.mewcraft.wakame.attribute.RandomAttribute
import cc.mewcraft.wakame.item.SlotContent
import cc.mewcraft.wakame.item.TagSerializable
import cc.mewcraft.wakame.util.compoundBinaryTag
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.CompoundBinaryTag

/**
 * 元素攻击力
 */
class ElementAttackDamage(
    var id: String,
    min: Int,
    max: Int,
) : RandomAttribute(min, max), ItemAttribute, PlayerAttribute, TagSerializable {
    private val key = Key.key(SlotContent.ATTRIBUTE_NAMESPACE, "${id}_attack_damage")

    override fun key(): Key {
        return key
    }

    override fun asAttributeModifier(): AttributeModifier {
        throw UnsupportedOperationException()
    }

    // Short

    override fun save(): BinaryTag {
        return compoundBinaryTag {
            putShort("min", min.toShort())
            putShort("max", max.toShort())
        }
    }

    override fun load(tag: BinaryTag) {
        if (tag is CompoundBinaryTag) {
            min = tag.getShort("min").toInt()
            max = tag.getShort("max").toInt()
        }
    }
}