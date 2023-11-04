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
 * 中立攻击力
 *
 * When the player is holding an item that provides Attack Damage,
 * the values of this instance should directly replace the player's.
 */
class AttackDamage(
    min: Int,
    max: Int,
) : RandomAttribute(min, max), ItemAttribute, PlayerAttribute, TagSerializable {
    companion object {
        val KEY = Key.key(SlotContent.ATTRIBUTE_NAMESPACE, "attack_damage")
    }

    override fun key(): Key {
        return KEY
    }

    override fun asAttributeModifier(): AttributeModifier {
        throw UnsupportedOperationException()
    }

    // Short

    override fun save(): BinaryTag {
        return compoundBinaryTag {
            putShort("min", super.min.toShort())
            putShort("max", super.max.toShort())
        }
    }

    override fun load(tag: BinaryTag) {
        if (tag is CompoundBinaryTag) {
            super.min = tag.getShort("min").toInt()
            super.max = tag.getShort("max").toInt()
        }
    }
}
